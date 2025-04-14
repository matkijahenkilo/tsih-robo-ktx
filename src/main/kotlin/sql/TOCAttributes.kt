package org.matkija.bot.sql

//it is what it is, babygirl
object TOCAttributes {
    const val TABLE_NAME = "tocrooms"

    //attributes
    const val COLUMN_ROOM_ID = "roomid"

    val CREATE_TABLE_SCRIPT = """
        create table if not exists $TABLE_NAME ($COLUMN_ROOM_ID INTEGER);
    """.trimIndent()
}