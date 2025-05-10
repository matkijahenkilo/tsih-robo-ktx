package org.matkija.bot.discordBot.passiveCommands

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

fun randomReactInit(jda: JDA) {
    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.id == event.jda.selfUser.id || event.author.isBot) return@listener
        val c = event.message.contentRaw.lowercase()

        // todo: a fucking command that configs a default chance for each server
        if (Math.random() <= 0.0025 || c.contains("tsih") || c.contains("nora")) {
            val customEmoji = jda.guilds.random().emojis.random()
            event.message.addReaction(customEmoji).queue()
        }
    }
}