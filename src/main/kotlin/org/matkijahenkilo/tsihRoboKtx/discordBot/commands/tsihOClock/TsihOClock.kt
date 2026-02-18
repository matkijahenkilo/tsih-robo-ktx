package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.tsihOClock

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil
import org.matkijahenkilo.tsihRoboKtx.sql.TOCChannel

class TsihOClock(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {

    override fun execute() {
        val option = event.getOption(OPTION_ACTION)!!.asInt
        if (option == 1) {
            saveRoomId()
        } else if (option == 0) {
            deleteRoomId()
        }
    }

    private fun saveRoomId() {
        val channelId = event.channelIdLong

        val allTsihOClockRooms = JPAUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            event.reply("This room is already subscribed to receive my awesome images nanora!").queue()
        } else {
            JPAUtil.saveTsihOClockRoom(TOCChannel(channelId = channelId))
            event.reply("Done nanora!").queue()
        }
    }

    private fun deleteRoomId() {
        val channelId = event.channelIdLong

        val allTsihOClockRooms = JPAUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            JPAUtil.deleteTsihOClockRoomById(channelId)
            event.reply("Done nanora! You're free from my beauty..............").queue()
        } else {
            event.reply("You're not even subscribed!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun isRoomAlreadySaved(roomId: Long, savedIds: List<TOCChannel>): Boolean =
        savedIds.any { it.channelId == roomId }

    companion object SlashCommands {
        const val TSIH_O_CLOCK = "tsihoclock"
        const val OPTION_ACTION = "action"

        fun getCommands(): SlashCommandData =
            Commands.slash(TSIH_O_CLOCK, "I'll send a scheduled image everyday nanora~")
                .addOptions(
                    OptionData(OptionType.INTEGER, OPTION_ACTION, "Should I save this room to send images?~", true)
                        .addChoice("subscribe", 1)
                        .addChoice("unsubscribe", 0)
                )
    }
}