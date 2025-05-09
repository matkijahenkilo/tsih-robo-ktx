package org.matkija.bot.discordBot.hybridCommands.markov

import java.io.File

class HistoryBuffer(private val guildId: Long, channelId: Long?) {
    private val file: File = File("data/markov/${guildId}_${channelId}")
    private val workingDir: File = File("data/markov")

    init {
        if (!workingDir.exists()) workingDir.mkdir()
    }

    fun appendToFile(content: String) = file.appendText(" $content")

    fun getChannelsTextsBelongingToGuild(): String? {
        val m = mutableListOf<String>()
        workingDir.listFiles()!!.forEach { file ->
            if (file.name.contains(guildId.toString())) {
                m.add(file.readText().trim())
            }
        }
        return if (m.isNotEmpty()) m.joinToString() else null
    }

    fun fileDoesNotExist(): Boolean = !this.file.exists()

    fun deleteFile() = file.delete()
}