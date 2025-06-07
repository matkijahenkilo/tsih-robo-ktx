package org.matkija.bot.discordBot.passiveCommands

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.matkija.bot.discordBot.CommandsEnum
import org.matkija.bot.sql.jpa.PersistenceUtil
import kotlin.random.Random

fun randomReactInit(jda: JDA) {

    val defaultChance = 0.025F

    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.id == event.jda.selfUser.id || event.author.isBot) return@listener
        val c = event.message.contentRaw.lowercase()

        val chanceEntity = PersistenceUtil.getCustomChanceEntity(event.guild.idLong)
        val chance = chanceEntity?.eventRandomReactChance ?: defaultChance
        if (Random.nextFloat() * 100 <= chance || c.contains("tsih") || c.contains("nora")) {
            val customEmoji = jda.guilds.random().emojis.random()
            event.message.addReaction(customEmoji).queue()
        }
    }
}