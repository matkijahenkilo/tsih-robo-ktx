package org.matkija.bot.discordBot.hybridCommands.markov

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandler
import org.matkija.bot.discordBot.hybridCommands.markov.slash.MarkovRoomHandlerSlashCommands
import org.matkija.bot.sql.JPAUtil
import org.matkija.bot.sql.MarkovAllowedChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/*
I hate this place lmao
 */
fun markovPassiveInit(jda: JDA, shouldResetMarkovFiles: Boolean): SlashCommandData {

    val defaultChance = 5.0F // 5%
    val logger: Logger = LoggerFactory.getLogger("MarkovInit")
    val markovsMap: MutableMap<Long, MarkovChain> = mutableMapOf() // key is the guild's id
    var savedMarkovChannels: List<MarkovAllowedChannel> = JPAUtil.getAllMarkovInfo()
    val quotesPattern = Regex("\"(.+)\"")

    if (shouldResetMarkovFiles) {
        logger.info("-m Argument used, deleting everything in data/markov/")
        CorpusSaverManager(null, null).workingDir.listFiles()?.forEach { file: File? ->
            file?.delete()
        }
        logger.info("Deleted everything")
    }

    fun saveUnsavedChannelsToDisk() {
        savedMarkovChannels.forEach { markovChannel ->
            if (markovChannel.readingChannelId != null) {
                val textChannelObj = jda.getTextChannelById(markovChannel.readingChannelId)
                if (textChannelObj != null) {
                    val hb = CorpusSaverManager(markovChannel.guildId, textChannelObj.idLong)
                    if (hb.fileDoesNotExist()) {
                        logger.info("Fetching messages of channel #${textChannelObj.name} from Discord")

                        val textFromChannel = mutableListOf<String>()

                        textChannelObj.getHistoryBefore(textChannelObj.latestMessageIdLong, 100)
                            .complete().retrievedHistory.forEach {
                                // do not save messages created by bots or that mentions this jda instance
                                if (!it.author.isBot || !it.contentRaw.contains(jda.selfUser.asMention))
                                    textFromChannel.add(it.contentRaw)
                            }

                        hb.appendToFile(textFromChannel.joinToString(" ").clearForMarkovCorpus())
                    } else {
                        logger.info("Channel #${textChannelObj.name} is already saved to disk")
                    }
                }
            }
        }
    }

    fun saveCurrentGuildCorpusFromDiskToMap(guildId: Long) {
        val textsBelongingToGuild = CorpusSaverManager(guildId, null).getChannelsTextsBelongingToGuild()
        if (textsBelongingToGuild != null) {
            markovsMap[guildId] = MarkovChain(textsBelongingToGuild.clearForMarkovCorpus().split(" "))
        } else {
            markovsMap.remove(guildId) // remove if there's no file/nothing to read
        }
    }

    fun replaceEntireMapWithCorpusFromDisk() {
        savedMarkovChannels.map { it.guildId }.distinct().forEach {
            saveCurrentGuildCorpusFromDiskToMap(it)
        }
    }

    logger.info("Saving texts from ${savedMarkovChannels.size} guilds to Markov Text generator")

    saveUnsavedChannelsToDisk()
    replaceEntireMapWithCorpusFromDisk()

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
            // log only if bot is not mentioned in message
            if (!contentRaw.contains(jda.selfUser.asMention)) {
                val textCleared = contentRaw.clearForMarkovCorpus()
                CorpusSaverManager(guildId, channelId).appendToFile(textCleared)
                markov?.appendCorpus(textCleared.split(" "))
            }
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

            val chanceEntity =
                JPAUtil.getCustomChanceEntity(event.guild.idLong)
            val chance = chanceEntity?.eventMarkovTextChance ?: defaultChance
            if (Random.nextFloat() * 100 <= chance || isForced) {
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
        when {
            !event.isFromGuild -> {
                event.reply("This only work in servers nanora!").queue()
                return@onCommand
            }

            event.channelType.isThread -> {
                event.reply("This doesn't work on threads nora.").queue()
                return@onCommand
            }

            event.channelType.isAudio -> {
                event.reply("This doesn't work on voice channels nora.").queue()
                return@onCommand
            }
        }

        MarkovRoomHandler(event).tryExecute()

        savedMarkovChannels = JPAUtil.getAllMarkovInfo()

        if (event.subcommandName == MarkovRoomHandlerSlashCommands.OPTION_READ) {
            // will save if a channel is added, or ignored if it was removed
            saveUnsavedChannelsToDisk()
            // content in disk is altered, so update it in memory as well
            saveCurrentGuildCorpusFromDiskToMap(event.guild!!.idLong)
        }
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
    val updateMarkovMapTask = Runnable {
        // if there's no internet and this function runs, everything will be null and everything will be deleted lmao (* ^ ω ^)
        if (jda.status == JDA.Status.CONNECTED) {
            logger.info("Scheduler: Clearing deleted channels from database and corpus from disk")
            var c = 0
            JPAUtil.getAllMarkovInfo().forEach {
                if (it.writingChannelId != null) {
                    val textChannelById = jda.getTextChannelById(it.writingChannelId)
                    if (textChannelById == null && jda.status == JDA.Status.CONNECTED) {
                        JPAUtil.deleteMarkovWritingChannelById(it.writingChannelId)
                        CorpusSaverManager(it.guildId, it.writingChannelId).deleteFile()
                        c++
                    }
                }
                if (it.readingChannelId != null && jda.status == JDA.Status.CONNECTED) {
                    val textChannelById = jda.getTextChannelById(it.readingChannelId)
                    if (textChannelById == null) {
                        JPAUtil.deleteMarkovReadingChannelById(it.readingChannelId)
                        CorpusSaverManager(it.guildId, it.readingChannelId).deleteFile()
                        c++
                    }
                }
            }
            if (c > 0)
                logger.info("Scheduler: Deleted $c entries from db and disk")
            else
                logger.info("Scheduler: Nothing to be cleared")

            logger.info("Scheduler: Updating Markov map")
            replaceEntireMapWithCorpusFromDisk()
            logger.info("Scheduler: Done")
        } else {
            logger.info("Scheduler: No internet, skipping cleanup")
        }
    }

    // run every 6 hours
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(updateMarkovMapTask, 6, 6, TimeUnit.HOURS)


    return MarkovRoomHandlerSlashCommands.getCommands()
}