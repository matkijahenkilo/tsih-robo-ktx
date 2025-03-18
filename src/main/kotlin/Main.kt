package org.matkija.bot

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.matkija.bot.discordBot.commands.music.*
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import org.matkija.bot.discordBot.sauceSender.SauceSender
import org.matkija.bot.discordBot.sauceSender.canIFixIt
import java.io.File
import java.util.logging.Logger
import kotlin.system.exitProcess


@Serializable
data class Bot(val name: String, val token: String)

private fun getBotsConfig(): List<Bot>? {
    try {
        return Json.decodeFromString<List<Bot>>(File("data/config.json").readText())
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun main() {

    val bots = getBotsConfig() ?: exitProcess(2)

    val bot = bots[0]

    println("Logging in as ${bot.name}")
    val jda = default(bot.token) {
        intents += listOf(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES
        )
    }
    jda.awaitReady()
    val ownerId = jda.retrieveApplicationInfo().complete().owner.id
//    val dataBaseHandler = DatabaseHandler(bot.name)

    /*
    fix links
     */
    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.id == event.jda.selfUser.id) return@listener
        if (event.author.isBot) return@listener
        val c = event.message.contentRaw.replace("\n", " ").replace("||", "")

        if (c.contains("https://"))
            if (canIFixIt(c)) {
                try {
                    SauceSender(event, c).sendSauce()
                } catch (e: Exception) {
                    println(e)
                }
            }
    }

    /*
    random react
     */
    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.id == event.jda.selfUser.id) return@listener
        val c = event.message.contentRaw

        if (Math.random() <= 0.02 || c.contains("tsih") || c.contains("nora"))
            event.message.addReaction(event.guild.emojis.random()).queue()
    }

    /*
    load command listeners and return their slash commands
     */
    val commandList = listOf(
        musicInit(jda)
    )
    val updateCommands = jda.updateCommands()
    commandList.forEach {
        updateCommands.addCommands(it)
    }
    updateCommands.queue()
}