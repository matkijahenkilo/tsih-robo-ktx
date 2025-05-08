package org.matkija.bot.discordBot.hybridCommands.markov.slash

import dev.minn.jda.ktx.interactions.commands.Subcommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object MarkovRoomHandlerSlashCommands {
    const val MARKOV = "markov"
    const val OPTION_READ = "read"
    const val OPTION_TALK = "talk"
    private const val OPTION_ACTION = "action"

    fun getCommands(): SlashCommandData =
        Commands.slash(MARKOV, "Markov Bot commands nanora~")
            .addSubcommands(
                Subcommand(OPTION_READ, "!!!THIS WILL LOG THIS CHANNEL!!! And also feed my Markov vocabulary!")
                    .addOptions(
                        OptionData(
                            OptionType.INTEGER,
                            OPTION_ACTION,
                            "Should I use this room as a model and log it during my runtime nora?",
                            true
                        )
                            .addChoice("yes, log this channel", 1)
                            .addChoice("no, stop logging this channel", 0)
                    ),
                Subcommand(OPTION_TALK, "With my vocabulary formed, I will spit out words in this channel nora~")
                    .addOptions(
                        OptionData(
                            OptionType.INTEGER,
                            OPTION_ACTION,
                            "Should I talk is this channel nora?",
                            true
                        )
                            .addChoice("yeah, talk gibberish!", 1)
                            .addChoice("SHUT THE FUCK UP!!!", 0)
                    )
            )
}