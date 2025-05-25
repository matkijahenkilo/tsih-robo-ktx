package org.matkija.bot.discordBot.commands.chanceManager

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.CommandsEnum

object ChanceManagerSlashOptions {

    const val PERCENTAGE = "chance_manager"
    const val PERCENTAGE_OPTION_FIELD = "percentage"
    const val PERCENTAGE_OPTION_EVENT = "event"

    fun getCommands(): SlashCommandData =
        Commands.slash(PERCENTAGE, "I'll set a custom percentage of an event nanora!")
            .addOptions(
                OptionData(OptionType.INTEGER, PERCENTAGE_OPTION_EVENT, "For which event nanora?", true)
                    .addChoice("Random reactions", CommandsEnum.RANDOM_REACT.ordinal.toLong())
                    .addChoice("Markov Chain text", CommandsEnum.MARKOV.ordinal.toLong()),
                OptionData(OptionType.NUMBER, PERCENTAGE_OPTION_FIELD, "Input da percentage nora!", true)
            )
}