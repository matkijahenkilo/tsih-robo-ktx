package org.matkija.bot.discordBot.commands.avatar

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun avatarInit(jda: JDA): SlashCommandData {
    jda.onCommand(AvatarSlashOptions.AVATAR) { event ->
        Avatar(event).tryExecute()
    }

    return AvatarSlashOptions.getCommands()
}