package org.matkija.bot.sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DatabaseHandler(dbName: String) {

    private val connection: Connection

    init {
        val url = "jdbc:sqlite:%s.db".format(dbName)
        connection = DriverManager.getConnection(url)
        val statements = listOf(
            TOCAttributes.CREATE_TABLE_SCRIPT
        ).joinToString(";")
        runUpdate(statements)
    }

    fun runUpdate(statement: String) {
        val st = connection.prepareStatement(statement)
        st.executeUpdate()
        st.close()
    }

    fun getResult(statement: String): ResultSet? {
        val st = connection.createStatement()
        return st.executeQuery(statement)
    }
}