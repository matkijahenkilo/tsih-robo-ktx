package org.matkija.bot.discordBot.commands.avatar

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object AvatarSlashOptions {
    const val AVATAR = "avatar"
    const val AVATAR_OPTION_USER = "user"

    fun getCommands(): SlashCommandData =
        Commands.slash(AVATAR, "I'll fetch your or someone else's avatar nanora!")
            .addOptions(
                OptionData(OptionType.USER, AVATAR_OPTION_USER, "Input your question here nanora!", false)
            )
}