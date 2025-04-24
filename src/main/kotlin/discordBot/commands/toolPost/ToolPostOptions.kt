package discordBot.commands.toolPost

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object ToolPostOptions {
    const val TOOLPOST = "toolpost"
    const val TOOLPOST_OPTION_LINK = "link"
    const val TOOLPOST_OPTION_TRIM = "climax"

    fun getCommands(): SlashCommandData =
        Commands.slash(TOOLPOST, "I'll make a toolpost out of an audio nanora!")
            .addOptions(
                OptionData(
                    OptionType.STRING,
                    TOOLPOST_OPTION_LINK,
                    "Input your link nanora!",
                    true
                ),
                OptionData(
                    OptionType.NUMBER,
                    TOOLPOST_OPTION_TRIM,
                    "The timestamp in seconds where the climax of the song occurs!",
                    false
                )
            )
}