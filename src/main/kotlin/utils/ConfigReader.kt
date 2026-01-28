package org.matkija.bot.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

object ConfigReader {
    private val logger = LoggerFactory.getLogger(ConfigReader.javaClass.name)
    private val workingFile = File("data/commandConfigs.json")

    lateinit var configs: Configs

    @Serializable
    class Configs(
        val markovWordLimit: Int
    )

    init {
        if (workingFile.exists()) {
            try {
                configs = Json.decodeFromString<Configs>(workingFile.readText())
                logger.info("Successfully loaded ${workingFile.name} ")
            } catch (e: Exception) {
                logger.warn("Configs were not loaded: ${e.toString()}")
                logger.warn("Loading default configs instead. Please fix ${workingFile.name}")
                loadDefaultConfig()
            }
        } else {
            writeDefaultConfigsToJson()
        }
    }

    private fun writeDefaultConfigsToJson() {
        loadDefaultConfig()
        workingFile.writeText(Json.encodeToString(configs))
    }

    private fun loadDefaultConfig() {
        configs = Configs(
            markovWordLimit = 10000
        )
    }
}