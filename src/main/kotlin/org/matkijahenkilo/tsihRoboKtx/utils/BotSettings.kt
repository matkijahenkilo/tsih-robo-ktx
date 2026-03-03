package org.matkijahenkilo.tsihRoboKtx.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object BotSettings {
    private val logger = LoggerFactory.getLogger(BotSettings.javaClass.name)
    private val propertiesFile = File("data/settings.properties")
    private val p = Properties()

    private enum class Settings(val propertyName: String, val defaultValue: String) {
        MARKOV_WORD_LIMIT("markovWordLimit", "10000"),
    }

    init {
        if (!propertiesFile.exists()) {
            logger.warn("${propertiesFile.name} does not exist, creating new file with default values.")
            setDefaultSettings()
            propertiesFile.outputStream().use { p.store(it, null) }
        } else {
            propertiesFile.inputStream().use { p.load(it) }
            addMissingProperties()
        }
    }

    private fun setDefaultSettings() = Settings.entries.forEach {
        p.setProperty(it.propertyName, it.defaultValue)
    }

    private fun addMissingProperties() {
        var hasChanged = false
        Settings.entries.forEach {
            if (!p.entries.any { propEntry -> propEntry.key == it.propertyName }) {
                logger.warn("${it.propertyName} does not exist in ${propertiesFile.name}, adding property to file with default value of ${it.defaultValue}.")
                p.setProperty(it.propertyName, it.defaultValue)
                hasChanged = true
            }
        }
        if (hasChanged) propertiesFile.outputStream().use { p.store(it, null) }
    }

    fun getMarkovWordLimit() =
        p.getProperty(Settings.MARKOV_WORD_LIMIT.propertyName, Settings.MARKOV_WORD_LIMIT.defaultValue).toInt()
}