package org.matkija.bot.discordBot.commands.chanceManager

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.CommandsEnum
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.sql.jpa.CustomChance
import org.matkija.bot.sql.jpa.PersistenceUtil

class ChanceManager(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val commandEntryNumber = event.getOption(ChanceManagerSlashOptions.PERCENTAGE_OPTION_EVENT)!!.asInt
        var chance = event.getOption(ChanceManagerSlashOptions.PERCENTAGE_OPTION_FIELD)!!.asDouble.toFloat()
        val customChanceEntity = PersistenceUtil.getCustomChanceEntity(event.guild!!.idLong)

        if (chance < 0F)
            chance = 0F

        if (chance > 100F)
            chance = 100F

        when (commandEntryNumber) {
            CommandsEnum.RANDOM_REACT.ordinal -> {
                if (customChanceEntity == null) {
                    PersistenceUtil.saveOrUpdateCustomChance(
                        CustomChance(
                            guildId = event.guild!!.idLong,
                            eventRandomReactChance = chance
                        )
                    )
                } else {
                    customChanceEntity.eventRandomReactChance = chance
                    PersistenceUtil.saveOrUpdateCustomChance(customChanceEntity)
                }
            }

            CommandsEnum.MARKOV.ordinal -> {
                if (customChanceEntity == null) {
                    PersistenceUtil.saveOrUpdateCustomChance(
                        CustomChance(
                            guildId = event.guild!!.idLong,
                            eventMarkovTextChance = chance
                        )
                    )
                } else {
                    customChanceEntity.eventMarkovTextChance = chance
                    PersistenceUtil.saveOrUpdateCustomChance(customChanceEntity)
                }
            }
        }

        event.reply(
            "Done nanora! ${CommandsEnum.entries[commandEntryNumber].name}'s chance is now $chance% in this server!\n" +
                    "-# If the percentage was incorrect, be sure to use `.` instead of `,`"
        ).queue()
    }
}