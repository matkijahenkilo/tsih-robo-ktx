package org.matkija.bot.discordBot.commands.tsihOClock

import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.sql.DatabaseHandler
import org.matkija.bot.sql.TOCAttributes

class TsihOClock(private val db: DatabaseHandler) : SlashCommand() {

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
        val resultSet = db.getResult("select * from ${TOCAttributes.TABLE_NAME}")
        val savedIds = mutableListOf<Long>()
        if (resultSet != null) {
            while (resultSet.next())
                savedIds.add(resultSet.getLong(1))
        }

        if (isRoomAlreadySaved(channelId, savedIds)) {
            event.reply("This room is already subscribed to receive my awesome images nanora!").queue()
        } else {
            db.runUpdate("insert into ${TOCAttributes.TABLE_NAME} (${TOCAttributes.COLUMN_ROOM_ID}) values ($channelId)")
            event.reply("Done nanora!").queue()
        }
    }

    private fun deleteRoomId(event: GenericCommandInteractionEvent) {
        val channelId = event.channelId!!.toLong()
        val resultSet = db.getResult("select * from ${TOCAttributes.TABLE_NAME}")
        val savedIds = mutableListOf<Long>()
        if (resultSet != null) {
            while (resultSet.next())
                savedIds.add(resultSet.getLong(1))
        }

        if (isRoomAlreadySaved(channelId, savedIds)) {
            db.runUpdate("delete from ${TOCAttributes.TABLE_NAME} where ${TOCAttributes.COLUMN_ROOM_ID} == $channelId")
            event.reply("Done nanora! You're free from my beauty..............").queue()
        } else {
            event.reply("You're not even subscribed!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun isRoomAlreadySaved(roomId: Long, savedIds: List<Long>): Boolean = roomId in savedIds
}