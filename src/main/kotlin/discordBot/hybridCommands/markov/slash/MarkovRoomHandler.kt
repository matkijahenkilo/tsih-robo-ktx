package org.matkija.bot.discordBot.hybridCommands.markov.slash

import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.discordBot.hybridCommands.markov.passive.HistoryBuffer
import org.matkija.bot.sql.jpa.MarkovAllowedChannel
import org.matkija.bot.sql.jpa.PersistenceUtil

class MarkovRoomHandler(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val option = event.getOption(TOCSlashCommands.OPTION_ACTION)!!.asInt

        when (event.subcommandName) {
            MarkovRoomHandlerSlashCommands.OPTION_READ -> {
                if (option == 1) {
                    saveChannelIdToRead()
                } else if (option == 0) {
                    deleteChannelIdToRead()
                }
            }

            MarkovRoomHandlerSlashCommands.OPTION_TALK -> {
                if (option == 1) {
                    saveChannelIdToTalk()
                } else if (option == 0) {
                    deleteChannelIdToTalk()
                }
            }
        }
    }

    private fun saveChannelIdToTalk() {
        val channelId = event.channelIdLong
        val guildId = event.guild!!.idLong

        val allInfoFromThisGuild = PersistenceUtil.getMarkovInfoByGuildId(guildId)

        if (isWritingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            event.reply("I'm already using this channel to generate Markov text nanora!").queue()
        } else {
            PersistenceUtil.saveMarkovWritingChannel(
                MarkovAllowedChannel(
                    guildId = guildId,
                    writingChannelId = channelId
                )
            )
            event.reply("Done nanora! I'll be talking lots of nonsense here now nora~").queue()
        }
    }

    private fun deleteChannelIdToTalk() {
        val channelId = event.channelIdLong
        val guildId = event.guild!!.idLong

        val allInfoFromThisGuild = PersistenceUtil.getMarkovInfoByGuildId(guildId)

        if (isWritingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            PersistenceUtil.deleteMarkovWritingChannelById(channelId)
            event.reply("Okay nanora! I won't be saying any nonsense now nora~").queue()
        } else {
            event.reply("I'm not even saying anything!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun saveChannelIdToRead() {
        val channelId = event.channelIdLong
        val guildId = event.guild!!.idLong

        val allInfoFromThisGuild = PersistenceUtil.getMarkovInfoByGuildId(guildId)

        if (isReadingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            event.reply("I'm already using this channel to feed my Markov chain vocabulary nanora!").queue()
        } else {
            PersistenceUtil.saveMarkovReadingChannel(
                MarkovAllowedChannel(
                    guildId = guildId,
                    readingChannelId = channelId
                )
            )
            event.reply("I'll be using this channel to feed my Markov chain vocabulary nanora!").queue()
        }
    }

    private fun deleteChannelIdToRead() {
        val channelId = event.channelIdLong
        val guildId = event.guild!!.idLong

        val allInfoFromThisGuild = PersistenceUtil.getMarkovInfoByGuildId(guildId)

        if (isReadingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            PersistenceUtil.deleteMarkovReadingChannelById(channelId)
            event.reply("Done nanora! I won't be reading this chat anymore~").queue()

            // delete file if server is not in db anymore
            val serverIsUsingMarkov =
                PersistenceUtil.getAllMarkovInfo().contains(MarkovAllowedChannel(guildId = guildId))
            if (!serverIsUsingMarkov)
                HistoryBuffer(guildId).deleteFile()
        } else {
            event.reply("I'm not even reading it!!!").queue()
        }
    }

    private fun isWritingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.writingChannelId == channelId }

    private fun isReadingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.readingChannelId == channelId }
}