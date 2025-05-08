package org.matkija.bot.discordBot.hybridCommands.markov

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.hybridCommands.markov.passive.HistoryBuffer
import org.matkija.bot.discordBot.hybridCommands.markov.passive.MarkovChain
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandler
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandlerSlashCommands
import org.matkija.bot.sql.jpa.MarkovAllowedChannel
import org.matkija.bot.sql.jpa.PersistenceUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

fun markovPassiveInit(jda: JDA): SlashCommandData {

    val logger: Logger = LoggerFactory.getLogger("MarkovInit")
    val markovsMap: MutableMap<Long, MarkovChain> = mutableMapOf() // key is the guild's id
    var savedMarkovChannels: List<MarkovAllowedChannel> = PersistenceUtil.getAllMarkovInfo()

    // delete saved messages if server is not using markov at all
    HistoryBuffer(null).workingDir.listFiles()?.forEach { file ->
        if (savedMarkovChannels.any { it.guildId.toString() != file.nameWithoutExtension })
            file.delete()
    }

    fun updateMap(textChannelObj: TextChannel?, guild: Guild?) {
        if (textChannelObj != null && guild != null) {
            val historyBuffer = HistoryBuffer(guild.idLong)
            val textFromFile = historyBuffer.readFile()
            if (textFromFile == null) {
                logger.info("Fetching messages from Discord")

                val textFromChannel = mutableListOf<String>()

                textChannelObj.getHistoryBefore(textChannelObj.latestMessageIdLong, 100)
                    .complete().retrievedHistory.forEach {
                        if (!it.author.isBot)
                            textFromChannel.add(it.contentRaw)
                    }

                val clearedText = textFromChannel.joinToString(" ").clearForMarkovCorpus()

                historyBuffer.appendToFile(clearedText)
                markovsMap[guild.idLong] = MarkovChain(clearedText.split(" "))
            } else {
                logger.info("Fetching messages from disk")
                markovsMap[guild.idLong] = MarkovChain(textFromFile.split(" "))
            }
        }
    }

    fun updateEntireMap() {
        savedMarkovChannels.forEach { markovRoom ->
            if (markovRoom.readingChannelId != null) {
                val textChannel = jda.getTextChannelById(markovRoom.readingChannelId)
                logger.info("trying to fetch messages from: #${textChannel?.name}")
                val guild = textChannel?.guild
                updateMap(textChannel, guild)
            }
        }
    }

    logger.info("Saving texts from ${savedMarkovChannels.size} guilds to Markov Text generator")

    updateEntireMap()

    if (markovsMap.isEmpty()) {
        logger.warn("Markov Text generator wasn't loaded at all")
    } else {
        logger.info("Markov Text generator loaded for all servers!")
    }

    /*
    messageReceived listener
     */
    jda.listener<MessageReceivedEvent> { event ->
        if (event.author.isBot
            || !event.isFromGuild
            || event.message.contentRaw.isEmpty()
        )
            return@listener

        val guildId = event.guild.idLong
        val contentRaw = event.message.contentRaw
        val markov = markovsMap[guildId]

        // if bot is allowed to log messages
        if (savedMarkovChannels.any { it.readingChannelId == event.channel.idLong }) {
            val textCleared = contentRaw.clearForMarkovCorpus()
            HistoryBuffer(guildId).appendToFile(textCleared)
            markov?.appendCorpus(textCleared.split(" "))
        }

        // if bot is allowed to compose markov shitpost
        if (savedMarkovChannels.any { it.writingChannelId == event.channel.idLong }) {
            var customMarkovWord: String? = null
            var isForced = false

            if (contentRaw.contains(jda.selfUser.asMention)) {
                isForced = true
                val words = contentRaw.replace(jda.selfUser.asMention, "").trim()
                if (words.isNotEmpty()) customMarkovWord = words
            }

            if (Math.random() <= 0.005 || isForced) {
                val sentenceSize = Random.nextInt(5, 30)
                val sentence = markov?.generateSentence(customMarkovWord, sentenceSize)

                if (!sentence.isNullOrEmpty())
                    event.channel.send(content = sentence).queue()
                else
                    event.channel.send(
                        content = "-# I don't have this word in my model. Did you also run `/${MarkovRoomHandlerSlashCommands.MARKOV}` ?"
                    ).queue()
            }
        }
    }


    /*
    slash command listeners
     */
    jda.onCommand(MarkovRoomHandlerSlashCommands.MARKOV) { event ->
        if (!event.isFromGuild) event.reply("This only work in servers nanora!").queue()
        MarkovRoomHandler(event).tryExecute()
        savedMarkovChannels = PersistenceUtil.getAllMarkovInfo()
        updateEntireMap()
    }


    return MarkovRoomHandlerSlashCommands.getCommands()
}