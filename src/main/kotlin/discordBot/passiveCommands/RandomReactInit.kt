package org.matkija.bot.discordBot.passiveCommands

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.matkija.bot.sql.JPAUtil
import kotlin.random.Random

fun randomReactInit(jda: JDA) {

    val defaultChance = 0.25F // 0.25%

    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.id == event.jda.selfUser.id || event.author.isBot) return@listener
        val c = event.message.contentRaw.lowercase()

        val chanceEntity = JPAUtil.getCustomChanceEntity(event.guild.idLong)
        val chance = chanceEntity?.eventRandomReactChance ?: defaultChance
        if (Random.nextFloat() * 100 <= chance || c.contains("tsih") || c.contains("nanora")) {
            val customEmoji = jda.guilds.random().emojis.random()
            event.message.addReaction(customEmoji).queue()
        }
    }
}