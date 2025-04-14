package org.matkija.bot.discordBot.commands.tsihOClock

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onCommandAutocomplete
import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.sql.DatabaseHandler
import org.matkija.bot.sql.TOCAttributes

fun tsihOClockInit(jda: JDA, db: DatabaseHandler): SlashCommandData {

    db.runUpdate(TOCAttributes.CREATE_TABLE_SCRIPT)

    jda.onCommand(TOCSlashCommands.TSIH_O_CLOCK) { event ->
        if (event.guild == null)
            event.reply("This only work in servers nanora!").queue()
        TsihOClock(db).tryExecute(event)
    }

    jda.onCommandAutocomplete(TOCSlashCommands.TSIH_O_CLOCK) { event ->
        val option = OptionData(OptionType.INTEGER, TOCSlashCommands.OPTION_ACTION, "You decide!")
            .addChoice("subscribe", 1)
            .addChoice("unsubscribe", 0)
        event.replyChoices(option.choices).queue()
    }

    return TOCSlashCommands.getCommands()
}