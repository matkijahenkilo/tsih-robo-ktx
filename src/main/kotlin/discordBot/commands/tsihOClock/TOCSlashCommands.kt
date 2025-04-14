package discordBot.commands.tsihOClock

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object TOCSlashCommands {
    const val TSIH_O_CLOCK = "tsihoclock"
    const val OPTION_ACTION = "action"

    fun getCommands(): SlashCommandData =
        Commands.slash(TSIH_O_CLOCK, "I'll send a scheduled image everyday nanora~")
            .addOption(OptionType.INTEGER, OPTION_ACTION, "Should I save this room to send images or not~", true, true)
}