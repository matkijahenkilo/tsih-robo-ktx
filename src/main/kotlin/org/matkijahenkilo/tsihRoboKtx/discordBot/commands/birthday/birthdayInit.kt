package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.birthday

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun birthdayInit(jda: JDA): SlashCommandData {
    jda.onCommand(Birthday.BIRTHDAY) { event ->
        if (event.guild == null)
            event.reply("This only work in servers nanora!").queue()
        Birthday(event).tryExecute()
    }

    return Birthday.getCommands()
}