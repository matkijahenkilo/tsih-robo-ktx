package org.matkija.bot.discordBot.passiveCommands.sauceSender

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.matkija.bot.LOG
import java.io.File

fun sauceSenderInit(jda: JDA) {

    if (!File("data/cookies.txt").exists())
        LOG.warn("cookies.txt doesn't exist in data/, many websites will break if you don't have your cookies exported!")

    LOG.info("Clearing data/temp folder")
    File("./data/temp").listFiles()?.forEach { it.delete() }

    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.isBot) return@listener
        val c = event.message.contentRaw.replace("\n", " ").replace("||", "")

        if (c.contains("https://") && shouldIFixIt(c)) {
            try {
                SauceSender(event, c).sendSauce()
            } catch (e: Exception) {
                LOG.error(e.toString())
            }
        }
    }
}