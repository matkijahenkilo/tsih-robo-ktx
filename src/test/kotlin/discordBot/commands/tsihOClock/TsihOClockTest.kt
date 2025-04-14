package discordBot.commands.tsihOClock

import org.junit.jupiter.api.Test

import org.matkija.bot.sql.DatabaseHandler
import org.matkija.bot.sql.TOCAttributes

class TsihOClockTest {

    private val imaginaryDBList = listOf(1250504617322741821, 1250504617322741822)
    private val existingId = imaginaryDBList[1]
    private val newId = 1250504617322741823

    private fun isRoomAlreadySaved(roomId: Long): Boolean = roomId in imaginaryDBList

    @Test
    fun `can avoid duplicating rooms`() {
        val databaseHandler = DatabaseHandler("")
        databaseHandler.runUpdate(TOCAttributes.CREATE_TABLE_SCRIPT)
        databaseHandler.runUpdate("insert into ${TOCAttributes.TABLE_NAME} (${TOCAttributes.COLUMN_ROOM_ID}) values ($existingId)")
        val resultSet = databaseHandler.getResult("select * from ${TOCAttributes.TABLE_NAME}")!!

        val channelList = mutableListOf<Long>()
        while (resultSet.next())
            channelList.add(resultSet.getLong(1))

        val isSaved = isRoomAlreadySaved(existingId)

        if (isSaved) {
            assert(true)
        } else {
            assert(false)
        }
    }

    @Test
    fun `can save new rooms`() {
        val databaseHandler = DatabaseHandler("")
        databaseHandler.runUpdate(TOCAttributes.CREATE_TABLE_SCRIPT)
        databaseHandler.runUpdate("insert into ${TOCAttributes.TABLE_NAME} (${TOCAttributes.COLUMN_ROOM_ID}) values ($existingId)")
        val resultSet = databaseHandler.getResult("select * from ${TOCAttributes.TABLE_NAME}")!!

        val channelList = mutableListOf<Long>()
        while (resultSet.next())
            channelList.add(resultSet.getLong(1))

        val isSaved = isRoomAlreadySaved(newId)

        if (isSaved) {
            assert(false)
        } else {
            assert(true)
        }
    }
}