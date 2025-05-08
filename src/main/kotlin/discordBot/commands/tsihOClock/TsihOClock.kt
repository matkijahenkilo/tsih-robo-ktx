package org.matkija.bot.discordBot.commands.tsihOClock

import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.sql.jpa.PersistenceUtil
import org.matkija.bot.sql.jpa.TOCChannel

class TsihOClock(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {

    override fun execute() {
        val option = event.getOption(TOCSlashCommands.OPTION_ACTION)!!.asInt
        if (option == 1) {
            saveRoomId()
        } else if (option == 0) {
            deleteRoomId()
        }
    }

    private fun saveRoomId() {
        val channelId = event.channelId!!.toLong()

        val allTsihOClockRooms = PersistenceUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            event.reply("This room is already subscribed to receive my awesome images nanora!").queue()
        } else {
            PersistenceUtil.saveTsihOClockRoom(TOCChannel(channelId = channelId))
            event.reply("Done nanora!").queue()
        }
    }

    private fun deleteRoomId() {
        val channelId = event.channelId!!.toLong()

        val allTsihOClockRooms = PersistenceUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            PersistenceUtil.deleteTsihOClockRoomById(channelId)
            event.reply("Done nanora! You're free from my beauty..............").queue()
        } else {
            event.reply("You're not even subscribed!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun isRoomAlreadySaved(roomId: Long, savedIds: List<TOCChannel>): Boolean =
        savedIds.any { it.channelId == roomId }
}