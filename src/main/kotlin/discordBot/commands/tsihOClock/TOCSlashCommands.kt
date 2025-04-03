package discordBot.commands.tsihOClock

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object TOCSlashCommands {
    const val TSIH_O_CLOCK = "question"
    const val TSIH_O_CLOCK_SUBSCRIBE = "subscribe"
    const val TSIH_O_CLOCK_UNSUSCRIBE = "unsubscribe"

    fun getCommands(): SlashCommandData =
        Commands.slash(TSIH_O_CLOCK, "I'll send a scheduled image everyday nanora~")
            .addSubcommands(
                SubcommandData(
                    TSIH_O_CLOCK_SUBSCRIBE,
                    "I'll start sending Tsih O'Clock images in this channel nanora!"
                ),
                SubcommandData(TSIH_O_CLOCK_UNSUSCRIBE, "I'll stop sending Tsih O'Clock images in this channel nanora!")
            )
}