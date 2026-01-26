package org.matkija.bot.discordBot.hybridCommands.markov

import java.io.File

class CorpusSaverManager(private val guildId: Long?, channelId: Long?) {
    private val file: File = File("data/markov/${guildId}_${channelId}")
    val workingDir: File = File("data/markov")
    private val maxTotalWords = 1000

    init {
        if (!workingDir.exists()) workingDir.mkdirs()
    }

    fun appendToFile(content: String) {
        // Get existing words + new content
        val currentText = if (file.exists()) file.readText() else ""
        val allWords = ("$currentText $content")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        // Trim to the last $maxTotalWords words
        val limitedWords = if (allWords.size > maxTotalWords) {
            allWords.takeLast(maxTotalWords)
        } else {
            allWords
        }

        // Overwrite the file with the limited set
        file.writeText(limitedWords.joinToString(" "))
    }

    fun getChannelsTextsBelongingToGuild(): String? {
        val files = workingDir.listFiles() ?: return null
        val guildTexts = files
            .filter { it.name.contains(guildId.toString()) }
            .map { it.readText().trim() }

        return if (guildTexts.isNotEmpty()) guildTexts.joinToString(" ") else null
    }

    fun fileDoesNotExist(): Boolean = !this.file.exists()
    fun deleteFile() = file.delete()
}