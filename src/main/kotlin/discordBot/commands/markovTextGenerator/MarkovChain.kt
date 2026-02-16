package org.matkija.bot.discordBot.commands.markovTextGenerator

import org.matkija.bot.utils.ConfigReader

class MarkovChain(corpus: List<String>) {

    private val markovChain: MutableMap<String, ArrayDeque<String>> = linkedMapOf()
    private val maxTotalWords = ConfigReader.configs.markovWordLimit
    private var totalWordCount = 0

    init {
        appendCorpus(corpus)
    }

    // Build a map where each word maps to its possible next words
    fun appendCorpus(corpus: List<String>) {
        for (i in corpus.indices) {
            val key = corpus[i]
            val value = corpus.getOrElse(i + 1) { "" }

            val nextWords = markovChain.getOrPut(key) { ArrayDeque() }
            nextWords.addLast(value)
            totalWordCount++

            if (totalWordCount > maxTotalWords)
                removeOldestEntry()
        }
    }

    private fun removeOldestEntry() {
        val oldestKey = markovChain.keys.firstOrNull() ?: return
        val oldestList = markovChain[oldestKey]!!

        oldestList.removeFirst()
        totalWordCount--

        // if that key now has no words left, remove the key from the map
        if (oldestList.isEmpty()) {
            markovChain.remove(oldestKey)
        }
    }

    fun generateSentence(word: String? = null, length: Int): String {
        var currentWord = (word ?: markovChain.keys.randomOrNull())?.takeIf { it in markovChain } ?: return ""

        return buildString {
            repeat(length) {
                append(currentWord).append(" ")
                val nextWords = markovChain[currentWord]
                if (nextWords.isNullOrEmpty()) return@buildString
                currentWord = nextWords.random()
            }
        }.trim()
    }
}