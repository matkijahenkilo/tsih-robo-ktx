package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.birthday

import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.sql.BirthdayChannel
import org.matkijahenkilo.tsihRoboKtx.sql.BirthdayUser
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil
import org.matkijahenkilo.tsihRoboKtx.utils.getRandomColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes


class Birthday(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {

    val log: Logger = LoggerFactory.getLogger(Birthday::class.java.name)

    override fun execute() {

        event.deferReply().setEphemeral(true).queue()

        when (event.subcommandName) {
            BIRTHDAY_ADD -> {
                if (!isAdmin()) return else if (channelIsNotSet()) return
                addUser()
            }

            BIRTHDAY_REMOVE -> {
                if (!isAdmin()) return
                removeUser()
            }

            BIRTHDAY_LIST -> listUsersByGuild()

            BIRTHDAY_SET -> {
                if (!isAdmin()) return
                setChat()
            }
        }
    }

    private fun addUser() {
        val day = event.getOption(BIRTHDAY_OPTION_DAY)!!.asLong
        val month = event.getOption(BIRTHDAY_OPTION_MONTH)!!.asLong
        val user = event.getOption(BIRTHDAY_OPTION_USER)!!.asMember!!
        val userId = user.idLong
        val guildId = event.guild!!.idLong
        val savedSubscription =
            JPAUtil.getBirthdayUserSubscriptionFromGuildIdAndUserId(userId, guildId)

        if (savedSubscription?.birthdayUserId?.userId == userId) {
            event.hook.editMessage(
                content = "But the person was already added nanora!!"
            ).queue()
            return
        }

        JPAUtil.saveBirthdayUser(BirthdayUser(day = day, month = month, userId = userId), guildId)

        val currentChannel = if (savedSubscription == null) {
            event.jda.getTextChannelById(
                JPAUtil.getBirthdayUserSubscriptionFromGuildIdAndUserId(
                    userId,
                    guildId
                )!!.birthdayChannelId.channelId
            )!!
        } else {
            event.jda.getTextChannelById(savedSubscription.birthdayChannelId.channelId)!!
        }
        event.hook.editMessage(
            content = "Done nanora! ${user.nickname ?: user.user.name} has been added and I'm going to send them a happy birthday message in #${currentChannel.name}"
        ).queue()

        log.info("Saved user ${user.nickname ?: user.user.name} in ${event.guild!!.name}")
    }

    private fun removeUser() {
        val user = event.getOption(BIRTHDAY_OPTION_USER)!!.asMember!!
        val userId = user.idLong
        val guildId = event.guild!!.idLong
        val savedSubscription =
            JPAUtil.getBirthdayUserSubscriptionFromGuildIdAndUserId(userId, guildId)

        if (savedSubscription == null) {
            event.hook.editMessage(
                content = "I don't even know their birthday nanora!"
            ).queue()
            return
        }

        JPAUtil.deleteBirthdayUser(userId, guildId)

        event.hook.editMessage(
            content = "Oki, I forgor ${user.nickname ?: user.user.name}'s birthday..."
        ).queue()

        log.info("Deleted user ${user.nickname ?: user.user.name} from ${event.guild!!.name}")
    }

    private fun listUsersByGuild() {
        val guildId = event.guild!!.idLong
        val birthdays = JPAUtil.getBirthdayUserSubscriptionsFromGuildId(guildId)
        val pages = mutableSetOf<MessageEmbed>()
        var content = mutableListOf<String>()

        if (birthdays == null) {
            event.hook.editMessage(content = "No one in the list nanora!").queue()
            return
        }

        birthdays.forEach { birthdaySubscription ->
            /*
            TODO(make user retrieval faster)
                event.jda.retrieveUserById().complete() takes a long while to load all users, maybe put them in a cache?
             */
            val user = event.jda.retrieveUserById(birthdaySubscription.birthdayUserId.userId).complete()
            if (user != null) {
                val monthName = Months.entries[(birthdaySubscription.birthdayUserId.month - 1).toInt()].monthName
                content.add(
                    String.format(
                        "`%s/%s` - %s",
                        birthdaySubscription.birthdayUserId.day,
                        monthName,
                        user.name
                    )
                )
                if (content.size == ENTRY_LIMIT) {
                    pages.add(EmbedBuilder {
                        description = content.joinToString("\n")
                        color = getRandomColor()
                    }.build())
                    content = mutableListOf()
                }
            }
        }

        if (content.isNotEmpty()) {
            pages.add(EmbedBuilder {
                description = content.joinToString("\n")
                color = getRandomColor()
            }.build())
        }

        if (pages.isNotEmpty()) {
            event.hook.sendPaginator(pages = pages.toTypedArray(), expireAfter = 3.minutes).setEphemeral(true).queue()
        } else {
            event.hook.editMessage(content = "No one in the list!").queue()
        }
    }

    private fun setChat() {
        JPAUtil.setOrReplaceBirthdayNotificationChannel(
            BirthdayChannel(
                channelId = event.messageChannel.idLong,
                guildId = event.guild!!.idLong
            )
        )

        event.hook.editMessage(
            content = "Ogei nanora! I'll send happy birthday messages here from now on."
        ).queue()

        log.info("Set chat #${event.messageChannel.name} in ${event.guild!!.name}")
    }

    private fun channelIsNotSet(): Boolean {
        val birthdayNotificationChannelFromGuildId =
            JPAUtil.getBirthdayNotificationChannelFromGuildId(event.guild!!.idLong)

        if (birthdayNotificationChannelFromGuildId == null) {
            event.hook.editMessage(content = "No channel is set! use `/${BIRTHDAY} ${BIRTHDAY_SET}` in a channel nanora!")
                .queue()
            return true
        } else if (event.jda.getTextChannelById(birthdayNotificationChannelFromGuildId.channelId) == null) {
            event.hook.editMessage(content = "No channel is set or previous one is now deleted nanora! use `/${BIRTHDAY} ${BIRTHDAY_SET}` in a channel nanora!")
                .queue()
            return true
        }

        return false
    }

    private fun isAdmin(): Boolean {
        val isAdmin = event.member!!.hasPermission(Permission.MANAGE_SERVER)
        if (!isAdmin) {
            event.hook.editMessage(content = "I'm sorry non-admin, I'm afraid I can't do that.")
                .queue()
        }
        return isAdmin
    }


    companion object SlashCommands {
        private const val ENTRY_LIMIT: Int = 10

        const val BIRTHDAY = "birthday"
        const val BIRTHDAY_ADD = "add"
        const val BIRTHDAY_OPTION_DAY = "day"
        const val BIRTHDAY_OPTION_MONTH = "month"
        const val BIRTHDAY_OPTION_USER = "user"
        const val BIRTHDAY_REMOVE = "remove"
        const val BIRTHDAY_SET = "set_chat"
        const val BIRTHDAY_LIST = "list_birthdays"

        fun getCommands(): SlashCommandData =
            Commands.slash(BIRTHDAY, "Manages my list of birthdays nanora!").addSubcommands(
                Subcommand(BIRTHDAY_ADD, "Add the birthday of someone for me to remember on the day nora~")
                    .addOptions(
                        OptionData(OptionType.STRING, BIRTHDAY_OPTION_DAY, "Le day", true),
                        OptionData(OptionType.INTEGER, BIRTHDAY_OPTION_MONTH, "Le month", true)
                            .addChoices(
                                Command.Choice(Months.JANUARY.monthName, Months.JANUARY.monthNumber),
                                Command.Choice(Months.FEBRUARY.monthName, Months.FEBRUARY.monthNumber),
                                Command.Choice(Months.MARCH.monthName, Months.MARCH.monthNumber),
                                Command.Choice(Months.APRIL.monthName, Months.APRIL.monthNumber),
                                Command.Choice(Months.MAY.monthName, Months.MAY.monthNumber),
                                Command.Choice(Months.JUNE.monthName, Months.JUNE.monthNumber),
                                Command.Choice(Months.JULY.monthName, Months.JULY.monthNumber),
                                Command.Choice(Months.AUGUST.monthName, Months.AUGUST.monthNumber),
                                Command.Choice(Months.SEPTEMBER.monthName, Months.SEPTEMBER.monthNumber),
                                Command.Choice(Months.OCTOBER.monthName, Months.OCTOBER.monthNumber),
                                Command.Choice(Months.NOVEMBER.monthName, Months.NOVEMBER.monthNumber),
                                Command.Choice(Months.DECEMBER.monthName, Months.DECEMBER.monthNumber)
                            ),
                        OptionData(OptionType.USER, BIRTHDAY_OPTION_USER, "Da user~", true)
                    ),
                Subcommand(BIRTHDAY_REMOVE, "I'll forget the birthday date nora.")
                    .addOption(OptionType.USER, BIRTHDAY_OPTION_USER, "Da user~", true),
                Subcommand(
                    BIRTHDAY_SET,
                    "When used, I'll use this chat to send happy birthday messages nanora!"
                ),
                Subcommand(BIRTHDAY_LIST, "I'll show the current list of birthday celebrants in this server nora~")
            )
    }
}