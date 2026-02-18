package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.markovTextGenerator.slash

import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.markovTextGenerator.CorpusSaverManager
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil
import org.matkijahenkilo.tsihRoboKtx.sql.MarkovAllowedChannel
import org.matkijahenkilo.tsihRoboKtx.utils.getRandomColor

class MarkovRoomHandler(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val option = event.getOption(OPTION_ACTION)?.asInt

        when (event.subcommandName) {
            OPTION_READ -> {
                if (option == 1) {
                    saveChannelIdToRead()
                } else if (option == 0) {
                    deleteChannelIdToRead()
                }
            }

            OPTION_WRITE -> {
                if (option == 1) {
                    saveChannelIdToWrite()
                } else if (option == 0) {
                    deleteChannelIdToWrite()
                }
            }

            OPTION_STATUS -> {
                val infoByGuildId = JPAUtil.getMarkovInfoByGuildId(event.guild!!.idLong)

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

        val allInfoFromThisGuild = JPAUtil.getMarkovInfoByGuildId(guildId)

        if (isWritingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            event.reply("I'm already using this channel to generate Markov text nanora!").queue()
        } else {
            JPAUtil.saveMarkovWritingChannel(
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

        val allInfoFromThisGuild = JPAUtil.getMarkovInfoByGuildId(guildId)

        if (isWritingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            JPAUtil.deleteMarkovWritingChannelById(channelId)
            event.reply("Okay nanora! I won't be saying any nonsense now nora~").queue()
        } else {
            event.reply("I'm not even saying anything!!! go away! go awayyy!!!!").queue()
        }
    }

    private fun saveChannelIdToRead() {
        val channelId = event.channelIdLong
        val guildId = event.guild!!.idLong

        val allInfoFromThisGuild = JPAUtil.getMarkovInfoByGuildId(guildId)

        if (isReadingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            event.reply("I'm already using this channel to feed my Markov chain vocabulary nanora!").queue()
        } else {
            JPAUtil.saveMarkovReadingChannel(
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

        val allInfoFromThisGuild = JPAUtil.getMarkovInfoByGuildId(guildId)

        if (isReadingChannelAlreadySaved(channelId, allInfoFromThisGuild)) {
            JPAUtil.deleteMarkovReadingChannelById(channelId)
            event.reply("Done nanora! I won't be reading this chat anymore~").queue()

            CorpusSaverManager(guildId).deleteFile()
        } else {
            event.reply("I'm not even reading it!!!").queue()
        }
    }

    private fun isWritingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.writingChannelId == channelId }

    private fun isReadingChannelAlreadySaved(channelId: Long, savedIds: List<MarkovAllowedChannel>): Boolean =
        savedIds.any { it.readingChannelId == channelId }

    companion object SlashOptions {
        const val MARKOV = "markov"
        const val OPTION_READ = "read"
        const val OPTION_WRITE = "write"
        const val OPTION_STATUS = "status"
        private const val OPTION_ACTION = "action"

        fun getCommands(): SlashCommandData =
            Commands.slash(MARKOV, "Markov Bot commands nanora~")
                .addSubcommands(
                    Subcommand(OPTION_READ, "!!!THIS WILL LOG THIS CHANNEL!!! And also feed my Markov vocabulary!")
                        .addOptions(
                            OptionData(
                                OptionType.INTEGER,
                                OPTION_ACTION,
                                "Should I use this room as a model and log it during my runtime nora?",
                                true
                            )
                                .addChoice("yes, log this channel", 1)
                                .addChoice("no, stop logging this channel", 0)
                        ),
                    Subcommand(OPTION_WRITE, "With my vocabulary formed, I will spit out words in this channel nora~")
                        .addOptions(
                            OptionData(
                                OptionType.INTEGER,
                                OPTION_ACTION,
                                "Should I write is this channel nora?",
                                true
                            )
                                .addChoice("yeah, write gibberish!", 1)
                                .addChoice("SHUT THE FUCK UP!!!", 0)
                        ),
                    Subcommand(OPTION_STATUS, "Shows my current configuration in this server nora~")
                )
    }
}