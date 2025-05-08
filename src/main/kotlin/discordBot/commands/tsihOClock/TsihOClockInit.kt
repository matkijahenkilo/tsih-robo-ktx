package org.matkija.bot.discordBot.commands.tsihOClock

import dev.minn.jda.ktx.events.onCommand
import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun tsihOClockInit(jda: JDA): SlashCommandData {

    jda.onCommand(TOCSlashCommands.TSIH_O_CLOCK) { event ->
        if (event.guild == null)
            event.reply("This only work in servers nanora!").queue()
        TsihOClock(event).tryExecute()
    }

    return TOCSlashCommands.getCommands()
}