package org.matkija.bot

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import discordBot.commands.toolPost.toolPosterInit
import discordBot.timedEvents.tsihOClockTimer.TsihOClockTimer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.requests.GatewayIntent
import org.flywaydb.core.Flyway
import org.matkija.bot.discordBot.commands.avatar.avatarInit
import org.matkija.bot.discordBot.commands.music.musicInit
import org.matkija.bot.discordBot.commands.question.questionInit
import org.matkija.bot.discordBot.commands.tsihOClock.tsihOClockInit
import org.matkija.bot.discordBot.hybridCommands.markov.markovPassiveInit
import org.matkija.bot.discordBot.passiveCommands.randomReactInit
import org.matkija.bot.discordBot.passiveCommands.sauceSender.sauceSenderInit
import org.matkija.bot.discordBot.timedEvents.randomStatus.RandomStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds


@Serializable
data class Bot(val name: String, val token: String)

private fun getBotsConfig(): List<Bot>? {
    try {
        return Json.decodeFromString<List<Bot>>(File("data/config.json").readText())
    } catch (e: Exception) {
        LOG.error(e.toString())
        return null
    }
}

private fun tsihOClockExists(): Boolean {
    val path = File("data/images/tsihoclock/")
    return if (path.exists()) path.listFiles()!!.isNotEmpty() else false
}

val LOG: Logger = LoggerFactory.getLogger("Tsih")

fun main(args: Array<String>) {

    val bots = getBotsConfig()
    if (bots == null) {
        LOG.error("Bot config not provided.")
        exitProcess(1)
    }

    // creates/migrates db if necessary
    Flyway.configure()
        .dataSource(
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:tsih-robo.db"
                maximumPoolSize = 2
                leakDetectionThreshold = 10.seconds.inWholeMilliseconds
            })
        )
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
        .migrate()

    var shouldDeleteMarkovFiles = false

    if (args.isNotEmpty()) {
        if (args[0] == "-t") {
            YoutubeAudioSourceManager().useOauth2(null, false)
            LOG.info("After you get the token, save it to data/oauth.txt and restart me")
        } else if (args[0] == "-m") {
            shouldDeleteMarkovFiles = true
        }
        else {
            LOG.error("Unknown arguments.")
        }
    }

    val bot = bots[0]

    LOG.info("Logging in as ${bot.name}")

    val jda = default(bot.token) {
        intents += listOf(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES
        )
    }
    jda.awaitReady()

    LOG.info("Currently in the following servers:")
    jda.guilds.forEach {
        LOG.info("${it.name} (${it.id})")
    }

    /*
    load passive commands listeners
     */
    sauceSenderInit(jda)
    randomReactInit(jda)

    /*
    load slash command listeners and return their slash commands
     */
    val commandList = mutableListOf(
        musicInit(jda),
        questionInit(jda),
        avatarInit(jda),
        toolPosterInit(jda),
        markovPassiveInit(jda, shouldDeleteMarkovFiles)
    )
    if (tsihOClockExists()) {
        commandList.add(tsihOClockInit(jda)).also {
            TsihOClockTimer(jda).startScheduler(TimeUnit.HOURS, 0, 1)
        }
    } else {
        LOG.warn("images in data/images/tsihoclock/ are missing, avoiding creation of command. If you want to use it just insert images into that folder.")
    }
    val updateCommands = jda.updateCommands()
    commandList.forEach {
        updateCommands.addCommands(it)
    }
    updateCommands.queue()

    /*
    timed functions
     */
    RandomStatus(jda).startScheduler(TimeUnit.MINUTES, 0, 5)

    LOG.info("Ready!")
}