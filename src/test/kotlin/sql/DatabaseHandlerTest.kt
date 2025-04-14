package sql

import org.junit.jupiter.api.Test

import org.matkija.bot.sql.TOCAttributes
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DatabaseHandlerTest(dbName: String) {

    private val connection: Connection

    init {
        val url = "jdbc:sqlite:%s".format(dbName)
        connection = DriverManager.getConnection(url)
    }

    fun createTable(reset: Boolean) {
        val statements = listOf(
            TOCAttributes.CREATE_TABLE_SCRIPT
        ).joinToString(";")

        if (reset)
            runUpdate("DROP TABLE IF EXISTS ${TOCAttributes.TABLE_NAME}")
        runUpdate(statements)
    }

    fun runUpdate(statement: String) {
        val st = connection.prepareStatement(statement)
        st.executeUpdate()
        st.close()
    }

    fun getReturn(statement: String): ResultSet? {
        val st = connection.createStatement()
        return st.executeQuery(statement)
    }
}

class runthisshit() {
    @Test
    fun `can create table, values and return them`() {
        val v1 = 1250504617322741821
        val v2 = 1250504617322741822
        val v3 = 1250504617322741823

        val databaseHandler = DatabaseHandlerTest("")
        databaseHandler.createTable(true)
        databaseHandler.runUpdate("""
            insert into ${TOCAttributes.TABLE_NAME} (${TOCAttributes.COLUMN_ROOM_ID}) values ($v1), ($v2), ($v3);
        """.trimIndent())
        val resultSet = databaseHandler.getReturn("select * from ${TOCAttributes.TABLE_NAME}")!!

        val res = mutableListOf<Long>()
        while (resultSet.next()) {
            res.add(resultSet.getLong(1))
        }

        assert(res[0] == v1)
        assert(res[1] == v2)
        assert(res[2] == v3)
    }
}