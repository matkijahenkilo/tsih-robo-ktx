package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.avatar

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun avatarInit(jda: JDA): SlashCommandData {
    jda.onCommand(Avatar.AVATAR) { event ->
        Avatar(event).tryExecute()
    }

    return Avatar.getCommands()
}