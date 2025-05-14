package org.matkija.bot.discordBot.hybridCommands.markov.slash

import dev.minn.jda.ktx.messages.EmbedBuilder
import discordBot.commands.tsihOClock.TOCSlashCommands
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.discordBot.hybridCommands.markov.HistoryBuffer
import org.matkija.bot.sql.jpa.MarkovAllowedChannel
import org.matkija.bot.sql.jpa.PersistenceUtil
import org.matkija.bot.utils.getRandomColor

class MarkovRoomHandler(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val option = event.getOption(TOCSlashCommands.OPTION_ACTION)?.asInt

        when (event.subcommandName) {
            MarkovRoomHandlerSlashCommands.OPTION_READ -> {
                if (option == 1) {
                    saveChannelIdToRead()
                } else if (option == 0) {
                    deleteChannelIdToRead()
                }
            }

            MarkovRoomHandlerSlashCommands.OPTION_WRITE -> {
                if (option == 1) {
                    saveChannelIdToWrite()
                } else if (option == 0) {
                    deleteChannelIdToWrite()
                }
            }

            MarkovRoomHandlerSlashCommands.OPTION_STATUS -> {
                val infoByGuildId = PersistenceUtil.getMarkovInfoByGuildId(event.guild!!.idLong)

                if (infoByGuildId.isEmpty()) {
                    event.reply("I'm not writing or reading anything in this server, nora.").setEphemeral(true).queue()
                    return
                }

                val writingList = mutableListOf<String?>()
                val readingList = mutableListOf<String?>()

                infoByGuildId.forEach {
                    if (it.writingChannelId != null) {
                        val guildChannelById = event.jda.getGuildChannelById(it.writingChannelId)
                        writingList.add("# ${guildChannelById?.name}")
                    }
                    if (it.readingChannelId != null) {
                        val guildChannelById = event.jda.getGuildChannelById(it.readingChannelId)
                        readingList.add("# ${guildChannelById?.name}")
                    }
                }

                val embed = EmbedBuilder {
                    title = "Current status for my Markov Chain text generator:"
                    color = getRandomColor()
                    field {
                        name = "Writing text in:"
                        value = writingList.joinToString("\n")
                        inline = true
                    }
                    field {
                        name = "Reading text from:"
                        value = readingList.joinToString("\n")
                        inline = true
                    }
                    footer {
                        name = "nanora!"
                    }
                }.build()

                event.replyEmbeds(embed).setEphemeral(true).queue()
            }
        }
    }

    private fun saveChannelIdToWrite() {
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
            event.reply("Done nanora! I'll be writing lots of nonsense here now nora~").queue()
        }
    }

    private fun deleteChannelIdToWrite() {
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

            HistoryBuffer(guildId, channelId).deleteFile()
        } else {
            event.reply("I'm not even reading it!!!").queue()
        }
    }

    private fun isWritingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.writingChannelId == channelId }

    private fun isReadingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.readingChannelId == channelId }
}