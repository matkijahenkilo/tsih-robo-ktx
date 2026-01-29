package org.matkija.bot.discordBot.commands.avatar

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object AvatarSlashOptions {
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