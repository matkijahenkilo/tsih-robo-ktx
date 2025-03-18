package org.matkija.bot.discordBot.commands.music

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

class PlaylistJsonHandler(private val path: String) {

    @Serializable
    data class SongEntry(val link: String, val requester: Long)

    fun getPlaylist(): List<SongEntry> {
        val file = File(path)
        file.createNewFile()
        return Json.decodeFromString<List<SongEntry>>(file.readText())
    }

    fun setPlaylist(list: List<SongEntry>) {
        val file = File(path)
        file.createNewFile()
        file.writeText(Json.encodeToJsonElement(list).toString())
    }
}