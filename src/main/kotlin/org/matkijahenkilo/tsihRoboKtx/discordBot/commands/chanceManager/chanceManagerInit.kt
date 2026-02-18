package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.chanceManager

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun chanceManagerInit(jda: JDA): SlashCommandData {
    jda.onCommand(ChanceManager.PERCENTAGE) { event ->
        if (!event.isFromGuild) {
            event.reply("This command works only inside guilds nanora!").queue()
            return@onCommand
        }
        ChanceManager(event).tryExecute()
    }

    return ChanceManager.getCommands()
}