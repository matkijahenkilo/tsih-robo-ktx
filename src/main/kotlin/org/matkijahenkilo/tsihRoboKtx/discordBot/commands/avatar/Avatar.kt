package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.avatar

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.utils.getRandomColor

class Avatar(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val member = event.getOption(AVATAR_OPTION_MEMBER)?.asMember
        val asUser = event.getOption(AVATAR_OPTION_AS_USER)?.asBoolean ?: false
        val avatarUrl =
            (if (asUser) member?.user?.effectiveAvatarUrl else member?.effectiveAvatarUrl
                ?: event.user.effectiveAvatarUrl) + "?size=2048"
        event.reply_(
            embeds = listOf(
                EmbedBuilder {
                    title = "This is a nice avatar nanora!"
                    color = getRandomColor()
                    field {
                        name = "What a lazy adult!"
                        value = "They look like holding a lot of shiny stars..."
                    }
                    image = avatarUrl
                }.build()
            )
        ).queue()
    }

    companion object SlashOptions {
        const val AVATAR = "avatar"
        const val AVATAR_OPTION_MEMBER = "member"
        const val AVATAR_OPTION_AS_USER = "as_user"

        fun getCommands(): SlashCommandData =
            Commands.slash(AVATAR, "I'll fetch your or someone else's avatar nanora!")
                .addOptions(
                    OptionData(
                        OptionType.USER,
                        AVATAR_OPTION_MEMBER,
                        "The member's avatar as seen in this server nanora!",
                        false
                    ),
                    OptionData(
                        OptionType.BOOLEAN,
                        AVATAR_OPTION_AS_USER,
                        "If I should fetch the avatar of the user outside of this server nanora!",
                        false
                    )
                )
    }
}