package org.matkija.bot.discordBot.hybridCommands.markov.passive

import java.util.*

class MarkovChain(corpus: List<String>) {

    private val markovChain: MutableMap<String, MutableList<String>> = mutableMapOf()

    init {
        appendCorpus(corpus)
    }

    // Build a map where each word maps to its possible next words
    fun appendCorpus(corpus: List<String>) {
        for (i in 0..corpus.size - 2) {
            val key = corpus[i]
            val value = corpus.getOrElse(i + 1) { "" }

            if (!markovChain.containsKey(key)) {
                markovChain[key] = mutableListOf(value)
            } else {
                markovChain[key]?.add(value)
            }
        }
    }

    // Generate a new sentence based on the Markov Chain
    fun generateSentence(word: String? = null, length: Int): String {
        if (markovChain.isEmpty()) return ""

        val random = Random()
        var sentence = ""

        // Start with a word from the corpus (or any seed you like)
        var currentWord = word ?: markovChain.keys.random()  // Choose a random starting word
        currentWord = currentWord.trim()

        for (i in 0 until length) {
            if (!markovChain.containsKey(currentWord)) break

            val nextWords = markovChain[currentWord]!!

            // Randomly pick one of the possible next words
            sentence += "$currentWord "
            currentWord = nextWords[random.nextInt(nextWords.size)]
        }

        return sentence.trim()
    }
}