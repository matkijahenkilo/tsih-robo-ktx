package org.matkija.bot.discordBot.commands.birthday

import dev.minn.jda.ktx.interactions.commands.Subcommand
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object BirthdaySlashCommands {

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