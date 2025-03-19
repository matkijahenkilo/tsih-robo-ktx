package org.matkija.bot

import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.requests.GatewayIntent
import org.matkija.bot.discordBot.commands.avatar.avatarInit
import org.matkija.bot.discordBot.commands.music.musicInit
import org.matkija.bot.discordBot.commands.question.questionInit
import org.matkija.bot.discordBot.passiveCommands.randomReactInit
import org.matkija.bot.discordBot.passiveCommands.sauceSender.sauceSenderInit
import org.matkija.bot.utils.TsihPoggers
import java.io.File
import kotlin.system.exitProcess


@Serializable
data class Bot(val name: String, val token: String)

private fun getBotsConfig(): List<Bot>? {
    try {
        return Json.decodeFromString<List<Bot>>(File("data/config.json").readText())
    } catch (e: Exception) {
        TsihPoggers.POG.error(e.toString())
        return null
    }
}

fun main() {

    val bots = getBotsConfig() ?: exitProcess(2)

    val bot = bots[0]

    TsihPoggers.POG.info("Logging in as ${bot.name}")

    val jda = default(bot.token) {
        intents += listOf(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES
        )
    }
    jda.awaitReady()
    //val ownerId = jda.retrieveApplicationInfo().complete().owner.id
    // val dataBaseHandler = DatabaseHandler(bot.name)

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
    )
    val updateCommands = jda.updateCommands()
    commandList.forEach {
        updateCommands.addCommands(it)
    }
    updateCommands.queue()
}