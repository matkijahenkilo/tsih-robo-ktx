package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.chanceManager

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.sql.CustomChance
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil

class ChanceManager(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val commandEntryNumber = event.getOption(PERCENTAGE_OPTION_EVENT)!!.asInt
        var chance = event.getOption(PERCENTAGE_OPTION_FIELD)!!.asDouble.toFloat()
        val customChanceEntity = JPAUtil.getCustomChanceEntity(event.guild!!.idLong)

        if (chance < 0F)
            chance = 0F

        if (chance > 100F)
            chance = 100F

        when (commandEntryNumber) {
            CommandsEnum.RANDOM_REACT.ordinal -> {
                if (customChanceEntity == null) {
                    JPAUtil.saveOrUpdateCustomChance(
                        CustomChance(
                            guildId = event.guild!!.idLong,
                            eventRandomReactChance = chance
                        )
                    )
                } else {
                    customChanceEntity.eventRandomReactChance = chance
                    JPAUtil.saveOrUpdateCustomChance(customChanceEntity)
                }
            }

            CommandsEnum.MARKOV.ordinal -> {
                if (customChanceEntity == null) {
                    JPAUtil.saveOrUpdateCustomChance(
                        CustomChance(
                            guildId = event.guild!!.idLong,
                            eventMarkovTextChance = chance
                        )
                    )
                } else {
                    customChanceEntity.eventMarkovTextChance = chance
                    JPAUtil.saveOrUpdateCustomChance(customChanceEntity)
                }
            }
        }

        event.reply(
            "Done nanora! ${CommandsEnum.entries[commandEntryNumber].normalName}'s chance is now $chance% in this server!\n" +
                    "-# If the percentage was incorrect, be sure to use `.` instead of `,`"
        ).queue()
    }

    companion object SlashOptions {
        const val PERCENTAGE = "chance_manager"
        const val PERCENTAGE_OPTION_FIELD = "percentage"
        const val PERCENTAGE_OPTION_EVENT = "event"

        fun getCommands(): SlashCommandData =
            Commands.slash(PERCENTAGE, "I'll set a custom percentage of an event nanora!")
                .addOptions(
                    OptionData(OptionType.INTEGER, PERCENTAGE_OPTION_EVENT, "For which event nanora?", true)
                        .addChoice(CommandsEnum.RANDOM_REACT.normalName, CommandsEnum.RANDOM_REACT.ordinal.toLong())
                        .addChoice(CommandsEnum.MARKOV.normalName, CommandsEnum.MARKOV.ordinal.toLong()),
                    OptionData(OptionType.NUMBER, PERCENTAGE_OPTION_FIELD, "Input da percentage nora!", true)
                )
    }
}