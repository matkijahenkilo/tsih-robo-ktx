package org.matkija.bot.discordBot.commands.tsihOClock

import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.sql.jpa.PersistenceUtil
import org.matkija.bot.sql.jpa.TOCRoom

class TsihOClock : SlashCommand() {

    override fun execute(event: GenericCommandInteractionEvent) {
        val option = event.getOption(TOCSlashCommands.OPTION_ACTION)!!.asInt
        if (option == 1) {
            saveRoomId(event)
        } else if (option == 0) {
            deleteRoomId(event)
        }
    }

    private fun saveRoomId(event: GenericCommandInteractionEvent) {
        val channelId = event.channelId!!.toLong()

        val allTsihOClockRooms = PersistenceUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            event.reply("This room is already subscribed to receive my awesome images nanora!").queue()
        } else {
            PersistenceUtil.saveTsihOClockRoom(TOCRoom(roomId = channelId))
            event.reply("Done nanora!").queue()
        }
    }

    private fun deleteRoomId(event: GenericCommandInteractionEvent) {
        val channelId = event.channelId!!.toLong()

        val allTsihOClockRooms = PersistenceUtil.getAllTsihOClockRooms()

        if (isRoomAlreadySaved(channelId, allTsihOClockRooms)) {
            PersistenceUtil.deleteTsihOClockRoomById(channelId)
            event.reply("Done nanora! You're free from my beauty..............").queue()
        } else {
            event.reply("You're not even subscribed!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun isRoomAlreadySaved(roomId: Long, savedIds: List<TOCRoom>): Boolean =
        savedIds.any { it.roomId == roomId }
}