package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.markovTextGenerator

import org.matkijahenkilo.tsihRoboKtx.utils.ConfigReader
import java.io.File
import java.util.*

class CorpusSaverManager(guildId: Long?) {
    private val file: File = File("data/markov/${guildId}")
    val workingDir: File = File("data/markov")
    private val maxTotalWords = ConfigReader.configs.markovWordLimit

    init {
        if (!workingDir.exists()) workingDir.mkdirs()
    }

    fun appendToFile(content: String) {
        file.appendText("$content ")
        // Only trim the file if it grows double the limit to reduce IO frequency
        if (getFileWordCount() > maxTotalWords * 2)
            trimFile()
    }

    private fun getFileWordCount(): Int {
        if (fileDoesNotExist()) return 0
        return file.bufferedReader().use { reader ->
            var wordCount = 0
            val scanner = Scanner(reader)
            while (scanner.hasNext()) {
                scanner.next()
                wordCount++
            }
            wordCount
        }
    }

    private fun trimFile() {
        if (fileDoesNotExist()) return
        val words = file.readText().split(Regex("\\s+")).filter { it.isNotBlank() }
        file.writeText(
            words.takeLast(maxTotalWords).joinToString(
                separator = " ",
                postfix = " "
            )
        )
    }

    fun getChannelsTextsBelongingToGuild(): String? = file.readText().trim().takeIf { it.isNotBlank() }

    fun fileDoesNotExist(): Boolean = !file.exists()
    fun deleteFile() = file.delete()
}