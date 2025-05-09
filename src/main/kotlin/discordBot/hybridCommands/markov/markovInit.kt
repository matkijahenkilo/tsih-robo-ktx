package org.matkija.bot.discordBot.hybridCommands.markov

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.messages.send
import discordBot.commands.tsihOClock.TOCSlashCommands
import kotlinx.coroutines.Runnable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandler
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandlerSlashCommands
import org.matkija.bot.sql.jpa.MarkovAllowedChannel
import org.matkija.bot.sql.jpa.PersistenceUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun markovPassiveInit(jda: JDA): SlashCommandData {

    val logger: Logger = LoggerFactory.getLogger("MarkovInit")
    val markovsMap: MutableMap<Long, MarkovChain> = mutableMapOf() // key is the guild's id
    var savedMarkovChannels: List<MarkovAllowedChannel> = PersistenceUtil.getAllMarkovInfo()
    val quotesPattern = Regex("\"(.+)\"")

    fun reloadMapByGuildId(textChannelObj: TextChannel?, guild: Guild?) {
        if (textChannelObj != null && guild != null) {
            val historyBuffer = HistoryBuffer(guild.idLong, textChannelObj.idLong)
            val textFromFile = historyBuffer.getChannelsTextsBelongingToGuild()
            if (historyBuffer.fileDoesNotExist()) {
                logger.info("Fetching messages of channel #${textChannelObj.name} from Discord")

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
                logger.info("Fetching messages of channel #${textChannelObj.name} from disk")
                markovsMap[guild.idLong] = MarkovChain(textFromFile!!.split(" "))
            }
        }
    }

    fun reloadEntireMap() {
        savedMarkovChannels.forEach { markovChannel ->
            if (markovChannel.readingChannelId != null) {
                val textChannel = jda.getTextChannelById(markovChannel.readingChannelId)
                reloadMapByGuildId(textChannel, textChannel?.guild)
            }
        }
    }

    logger.info("Saving texts from ${savedMarkovChannels.size} guilds to Markov Text generator")

    reloadEntireMap()

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
        val channelId = event.message.channelIdLong
        val contentRaw = event.message.contentRaw
        val markov = markovsMap[guildId]

        // if bot is allowed to log messages
        if (savedMarkovChannels.any { it.readingChannelId == event.channel.idLong }) {
            val textCleared = contentRaw.clearForMarkovCorpus()
            HistoryBuffer(guildId, channelId).appendToFile(textCleared)
            markov?.appendCorpus(textCleared.split(" "))
        }

        // if bot is allowed to compose markov shitpost
        if (savedMarkovChannels.any { it.writingChannelId == event.channel.idLong }) {
            var customMarkovWord: String? = null
            var isForced = false

            if (contentRaw.contains(jda.selfUser.asMention)) {
                isForced = true
                val word = quotesPattern.find(contentRaw)?.groupValues?.get(1) // 0 is the full match including "
                if (word != null) {
                    customMarkovWord = word
                }
            }

            if (Math.random() <= 0.01 || isForced) {
                // check if it's a phrase, get a random word from it
                if (customMarkovWord != null && customMarkovWord.contains(' '))
                    customMarkovWord = customMarkovWord.split(' ').filter { !it.contains(' ') }.random()

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
        if (!event.isFromGuild) {
            event.reply("This only work in servers nanora!").queue()
            return@onCommand
        }
        if (event.channelType.isThread) {
            event.reply("This doesn't work on threads nora.").queue()
            return@onCommand
        }
        if (event.channelType.isAudio) {
            event.reply("This doesn't work on voice channels nora.").queue()
            return@onCommand
        }

        MarkovRoomHandler(event).tryExecute()

        savedMarkovChannels = PersistenceUtil.getAllMarkovInfo()
        val option = event.getOption(TOCSlashCommands.OPTION_ACTION)!!.asInt
        if (event.subcommandName == MarkovRoomHandlerSlashCommands.OPTION_READ && option == 1) // 1 is for saving
            reloadMapByGuildId(jda.getTextChannelById(event.channelIdLong), event.guild)
    }


    /*
    reload map from file every so and often

    why? because of how MarkovChain class works.

    imagine a sequence of 3 messages:
    1. owo
    2. very nice
    3. ちんぽがすき

    the bot will try to append these 3 messages to its corpus, but it won't work to add to its vocabulary because
    the messages are too short

    nothing comes after "owo", "nice" or "ちんぽがすき", the only word it'll append is "very" because
    it will understand that the next possible outcome of "very" is "nice"

    in the .txt file, these 3 messages will be appended and become "owo very nice ちんぽがすき",
    thus only "ちんぽがすき" won't get appended to its corpus:
    ["owo", "very", "nice"]
    it simply cannot be used as a starting point

    one day I'll think of some better solution for updating its corpus lol
     */
    val updateMarkovMapTask: Runnable = Runnable {
        logger.info("Scheduler: Updating Markov map")
        reloadEntireMap()
        logger.info("Scheduler: Done")
    }

    // run every 6 hours
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(updateMarkovMapTask, 6, 6, TimeUnit.HOURS)


    return MarkovRoomHandlerSlashCommands.getCommands()
}