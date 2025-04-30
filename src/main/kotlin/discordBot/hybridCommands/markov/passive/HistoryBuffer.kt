package org.matkija.bot.discordBot.hybridCommands.markov.passive

import java.io.File

class HistoryBuffer(guildId: Long?) {
    private val file: File = File("data/markov/$guildId.txt")
    val workingDir: File = File("data/markov")

    init {
        if (!workingDir.exists()) workingDir.mkdir()
    }

    fun appendToFile(content: String) = file.appendText(" $content")

    fun readFile(): String? = if (file.exists())
        file.readText()
    else
        null

    fun deleteFile() = file.delete()
}