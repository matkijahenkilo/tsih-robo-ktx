package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.managers.AudioManager
import org.matkijahenkilo.tsihRoboKtx.LOG
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.audio.GuildMusicManager
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil
import org.matkijahenkilo.tsihRoboKtx.utils.formatMillis
import kotlin.time.Duration.Companion.minutes

class Music(
    private val event: GenericCommandInteractionEvent,
    private val musicManager: GuildMusicManager,
    private val playerManager: AudioPlayerManager
) : SlashCommand(event) {

    override fun execute() {
        event.deferReply().queue()

        val originalOption = event.getOption(MUSIC_OPTION_LINK)?.asString
        val searchPrefix = event.getOption(MUSIC_OPTION_SEARCH)?.asString

        val option = originalOption?.let { linkOrSearch ->
            when {
                searchPrefix != null -> "$searchPrefix$linkOrSearch"
                !linkOrSearch.contains("https://") -> "${YTMSEARCH_PREFIX}$linkOrSearch"
                else -> linkOrSearch
            }
        }

        when (event.subcommandName) {
            MUSIC_PLAY -> {
                if (option != null) {
                    preSongLoad()

                    val requestedTrackInfoList = getSongsDependingOfOption(option)

                    play(
                        entries = requestedTrackInfoList,
                        isPriority = false,
                        shouldSaveToDb = true
                    )

                    postSongLoad(option, originalOption)
                }
            }

            MUSIC_PLAY_NEXT -> {
                if (option != null) {
                    preSongLoad()

                    val requestedTrackInfoList = getSongsDependingOfOption(option)

                    play(
                        entries = requestedTrackInfoList,
                        isPriority = true,
                        shouldSaveToDb = true
                    )

                    postSongLoad(option, originalOption)
                }
            }

            MUSIC_RESUME_QUEUE -> {
                if (musicManager.player.playingTrack != null) {
                    event.hook.editMessage(
                        content = "A playlist is already being played nanora."
                    ).queue()
                    return
                }

                event.hook.editMessage(content = "Resuming a saved playlist nora...")
                    .queue()

                val guildPlaylist = JPAUtil.getPlaylistsById(event.guild!!.id)

                if (guildPlaylist.isNotEmpty()) {
                    val requestedTrackInfoList = guildPlaylist.map {
                        RequestedTrackInfo(
                            loadAudioTracks(listOf(it.link))[0], // saved songs will never be a playlist link
                            event.jda.getUserById(it.requester),
                            event.jda.getGuildById(it.guildId)
                        )
                    }

                    play(
                        entries = requestedTrackInfoList,
                        isPriority = false,
                        shouldSaveToDb = false
                    )

                    event.hook.editMessage(content = "Resumed the saved playlist nanora!")
                        .queue()
                } else {
                    event.hook.editMessage(
                        content = "There's no playlist saved nanora! Use `/${MUSIC} ${MUSIC_PLAY}` to start a new one nora~"
                    ).queue()
                }
            }

            MUSIC_SHOW_QUEUE -> {
                val queueContents = musicManager.scheduler.priorityQueue + musicManager.scheduler.originalQueue
                val pages = mutableSetOf<MessageEmbed>()
                var content = mutableListOf<String>()
                var index = 1

                val totalTime = formatMillis(queueContents.sumOf { it.track!!.info.length })

                queueContents.forEach { audioContent ->
                    val time = formatMillis(audioContent.track!!.info.length)
                    content.add(
                        String.format(
                            "%s. [%s](%s) (%s) by %s",
                            index,
                            audioContent.track.info.title,
                            audioContent.track.info.uri,
                            time,
                            audioContent.requester!!.name
                        )
                    )
                    if (content.size == ENTRY_LIMIT || index == queueContents.size) {
                        val str = content.joinToString("\n")
                        var embed = EmbedBuilder {
                            title = "The next songs to be played..."
                            description = str
                            color = 0xff80fd
                            footer {
                                name = "Total duration of the playlist: $totalTime nora!"
                            }
                        }
                        embed = MusicInfoEmbed.fillFields(embed, musicManager.scheduler, musicManager.player.isPaused)
                        pages.add(embed.build())
                        content = mutableListOf()
                    }
                    index++
                }

                if (pages.isNotEmpty()) {
                    event.hook.sendPaginator(pages = pages.toTypedArray(), expireAfter = 5.minutes).queue()
                } else {
                    event.hook.editMessage(content = "Nothing in playlist nanora!").queue()
                }
            }
        }
    }

    private fun loadAudioTrackFromSearch(
        search: String
    ): List<AudioTrack> {

        val channel = event.messageChannel
        val trackList = mutableListOf<AudioTrack>()

        playerManager.loadItemSync(search, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                trackList.add(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                trackList.add(playlist.tracks[0])
            }

            override fun noMatches() {
                channel.sendMessage("No match for \"$search\" nora.").queue()
            }

            override fun loadFailed(exception: FriendlyException) {
                LOG.error(exception.stackTraceToString())
                channel.sendMessage("I couldn't play anything nora!\nreason: ${exception.message}")
                    .queue()
            }
        })

        if (musicManager.scheduler.isShuffled) musicManager.scheduler.shuffle(false)

        return trackList
    }

    private fun loadAudioTracks(
        links: List<String>
    ): List<AudioTrack> {
        val channel = event.messageChannel
        val trackList = mutableListOf<AudioTrack>()

        links.forEach { trackUrl ->
            playerManager.loadItemSync(trackUrl, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    trackList.add(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    playlist.tracks.forEach { track ->
                        if (track != null)
                            trackList.add(track)
                    }
                }

                override fun noMatches() {
                    channel.sendMessage("Nothing found in <$trackUrl> nora.").queue()
                }

                override fun loadFailed(exception: FriendlyException) {
                    LOG.error(exception.stackTraceToString())
                    channel.sendMessage("I couldn't play anything nora!\nreason: ${exception.message}")
                        .queue()
                }
            })
        }

        if (musicManager.scheduler.isShuffled) musicManager.scheduler.shuffle(false)

        return trackList
    }

    /**
     * Plays or queue an AudioTrack
     */
    private fun play(
        entries: List<RequestedTrackInfo>,
        isPriority: Boolean,
        shouldSaveToDb: Boolean,
    ) {
        connectToMemberVC(event.guild!!.audioManager, event.member!!)
        musicManager.scheduler.queueSongs(entries, isPriority, shouldSaveToDb)
    }

    private fun connectToMemberVC(audioManager: AudioManager, member: Member) {
        if (!audioManager.isConnected/* && !audioManager.isAttemptingToConnect()*/) {
            audioManager.openAudioConnection(member.voiceState!!.channel as AudioChannel)
        }
    }

    private fun preSongLoad() {
        event.hook.editMessage(content = "Loading song(s)...")
            .queue()

        // clear saved playlist since users can forget about it
        // this avoids the database from getting huge uwu
        if (musicManager.player.playingTrack == null)
            JPAUtil.deletePlaylistById(event.guild!!.idLong)
    }

    private fun postSongLoad(option: String, originalOption: String?) {
        if (SEARCH_PREFIXES_ARRAY.any { option.startsWith(it) }) {
            event.hook.editMessage(content = "Loaded the first song of result `$originalOption` nanora!")
                .queue()
        } else {
            event.hook.editMessage(content = "Loaded a [song or playlist](<${option}>) nanora!")
                .queue()
        }
    }

    private fun getSongsDependingOfOption(
        option: String
    ): List<RequestedTrackInfo> =
        if (SEARCH_PREFIXES_ARRAY.any { option.startsWith(it) })
            loadAudioTrackFromSearch(option).map {
                RequestedTrackInfo(
                    it,
                    event.member!!.user,
                    event.guild!!
                )
            }
        else
            loadAudioTracks(option.split(" ")).map {
                RequestedTrackInfo(
                    it,
                    event.member!!.user,
                    event.guild!!
                )
            }

    companion object SlashCommands {
        private const val ENTRY_LIMIT = 10
        private val SEARCH_PREFIXES_ARRAY = arrayOf(
            YTSEARCH_PREFIX,
            YTMSEARCH_PREFIX,
            SCSEARCH_PREFIX
        )

        const val MUSIC = "music"
        const val MUSIC_PLAY = "play"
        const val MUSIC_PLAY_NEXT = "play_next"

        const val MUSIC_OPTION_LINK = "url_or_search"
        const val MUSIC_OPTION_SEARCH = "website_for_search"
        const val MUSIC_RESUME_QUEUE = "resume_queue"
        const val MUSIC_SHOW_QUEUE = "show_queue"

        private const val ID_MUSIC = "music"
        const val PLAY = "$ID_MUSIC:play"
        const val STOP = "$ID_MUSIC:stop"
        const val SKIP = "$ID_MUSIC:skip"
        const val REPEAT = "$ID_MUSIC:repeat"
        const val SHUFFLE = "$ID_MUSIC:shuffle"

        const val YTSEARCH_PREFIX = "ytsearch:"
        const val YTMSEARCH_PREFIX = "ytmsearch:"
        const val SCSEARCH_PREFIX = "scsearch:"

        private fun linkInputOptionData(): OptionData = OptionData(
            OptionType.STRING,
            MUSIC_OPTION_SEARCH,
            "Forces a search in a specific website for the option $MUSIC_PLAY.",
            false
        ).addChoices(
            Command.Choice("YouTube", YTSEARCH_PREFIX),
            Command.Choice("YouTube Music", YTMSEARCH_PREFIX),
            Command.Choice("SoundCloud", SCSEARCH_PREFIX)
        )

        fun getCommands(): SlashCommandData =
            Commands.slash(MUSIC, "I search or play songs directly from a link nanora!").addSubcommands(
                Subcommand(MUSIC_PLAY, "I'll search or play something in voice chat nora!").addOptions(
                    OptionData(OptionType.STRING, MUSIC_OPTION_LINK, "I'll play a song in voice chat!", true),
                    linkInputOptionData()
                ),
                Subcommand(MUSIC_PLAY_NEXT, "Prioritizes a track to be played next nora~").addOptions(
                    OptionData(
                        OptionType.STRING,
                        MUSIC_OPTION_LINK,
                        "I'll prioritize this song to play next nanora!",
                        true
                    ),
                    linkInputOptionData()
                ),
                Subcommand(
                    MUSIC_RESUME_QUEUE,
                    "Resumes a saved playlist from the last time I've been in voice chat nora~"
                ),
                Subcommand(MUSIC_SHOW_QUEUE, "I show the current queue nora~")
            )
    }
}