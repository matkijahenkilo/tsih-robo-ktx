package org.matkija.bot.discordBot.passiveCommands.sauceSender

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun sauceSenderInit(jda: JDA) {

    val logger: Logger = LoggerFactory.getLogger("sauceSenderInit")

    if (!File("data/cookies.txt").exists())
        logger.warn("cookies.txt doesn't exist in data/, many websites will break if you don't have your cookies exported!")

    logger.info("Clearing data/temp folder")
    File("./data/temp").listFiles()?.forEach { it.delete() }

    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.isBot) return@listener
        val c = event.message.contentRaw.replace("\n", " ").replace("||", "")

        if (c.contains("https://") && shouldIFixIt(c)) {
            try {
                SauceSender(event, c).sendSauce()
            } catch (e: Exception) {
                logger.error(e.toString())
            }
        }
    }
}