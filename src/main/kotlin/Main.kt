package org.matkija.bot

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
import org.matkija.bot.discordBot.passiveCommands.randomReactInit
import org.matkija.bot.discordBot.passiveCommands.sauceSender.sauceSenderInit
import org.matkija.bot.discordBot.timedEvents.randomStatus.RandomStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


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

val LOG: Logger = LoggerFactory.getLogger("Tsih")

fun main(args: Array<String>) {

    // creates/migrates db if necessary
    Flyway.configure().dataSource("jdbc:sqlite:tsih-robo.db", "", "").load().migrate()

    if (args.isNotEmpty()) {
        if (args[0] == "-t") {
            YoutubeAudioSourceManager().useOauth2(null, false)
            LOG.info("After you get the token, save it to data/oauth.txt and restart me")
        } else {
            LOG.error("Unknown arguments.")
        }
    }

    val bots = getBotsConfig() ?: exitProcess(2)

    val bot = bots[0]

    LOG.info("Logging in as ${bot.name}")

    val jda = default(bot.token) {
        intents += listOf(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES
        )
    }
    jda.awaitReady()

    /*
    load passive commands listeners
     */
    sauceSenderInit(jda)
    randomReactInit(jda)

    /*
    load slash command listeners and return their slash commands
     */
    val commandList = listOf(
        musicInit(jda),
        questionInit(jda),
        avatarInit(jda),
        toolPosterInit(jda),
        tsihOClockInit(jda)
    )
    val updateCommands = jda.updateCommands()
    commandList.forEach {
        updateCommands.addCommands(it)
    }
    updateCommands.queue()

    /*
    timed functions
     */
    RandomStatus(jda).startScheduler(TimeUnit.MINUTES, 0, 5)
    TsihOClockTimer(jda).startScheduler(TimeUnit.HOURS, 0, 1)
}