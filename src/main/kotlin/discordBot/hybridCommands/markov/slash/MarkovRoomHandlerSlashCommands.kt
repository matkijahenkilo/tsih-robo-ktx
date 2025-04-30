package org.matkija.bot.discordBot.hybridCommands.markov.slash

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object MarkovRoomHandlerSlashCommands {
    const val MARKOV = "markov"
    const val OPTION_READ = "read"
    const val OPTION_TALK = "talk"
    const val OPTION_ACTION = "action"

    fun getCommands(): SlashCommandData =
        Commands.slash(MARKOV, "Markov Bot commands nanora~")
            .addSubcommands(
                SubcommandData(OPTION_READ, "!!!THIS WILL LOG THIS CHANNEL!!! And also feed my Markov vocabulary!")
                    .addOption(
                        OptionType.INTEGER,
                        OPTION_ACTION,
                        "Should I use this room as a model and log it during my runtime~?",
                        true,
                        true
                    ),
                SubcommandData(OPTION_TALK, "With my vocabulary formed, I will spit out words in this channel nora~")
                    .addOption(
                        OptionType.INTEGER,
                        OPTION_ACTION,
                        "Should I talk in this server nanora?",
                        true,
                        true
                    )
            )
}