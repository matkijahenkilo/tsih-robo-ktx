package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.matkija.bot.LOG
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import org.matkija.bot.sql.JPAUtil
import kotlin.time.Duration.Companion.minutes

class Music(
    private val event: GenericCommandInteractionEvent,
    private val musicManager: GuildMusicManager,
    private val playerManager: AudioPlayerManager
) : SlashCommand(event) {

    override fun execute() {
        event.deferReply().queue()

        var option = event.getOption(MusicSlashCommands.MUSIC_OPTION_LINK)?.asString
        val originalOption = option
        val website = event.getOption(MusicSlashCommands.MUSIC_OPTION_SEARCH)?.asString

        if (option != null) {
            if (website != null) {
                option = website + option
            } else if (!option.contains("https://")) {
                option = "$YT_SEARCH$option"
            }
        }

        when (event.subcommandName) {
            MusicSlashCommands.MUSIC_PLAY -> {
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

            MusicSlashCommands.MUSIC_PLAY_NEXT -> {
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

            MusicSlashCommands.MUSIC_RESUME_QUEUE -> {
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
                        isPriority = true,
                        shouldSaveToDb = false
                    )

                    event.hook.editMessage(content = "Resumed the saved playlist nanora!")
                        .queue()
                } else {
                    event.hook.editMessage(
                        content = "There's no playlist saved nanora! Use `/${MusicSlashCommands.MUSIC} ${MusicSlashCommands.MUSIC_PLAY}` to start a new one nora~"
                    ).queue()
                }
            }

            MusicSlashCommands.MUSIC_SHOW_QUEUE -> {
                val queueContents = musicManager.scheduler.priorityQueue + musicManager.scheduler.originalQueue
                val pages = mutableSetOf<MessageEmbed>()
                var content = mutableListOf<String>()
                var index = 1

                val totalTime = getTimestamp(queueContents.sumOf { it.track!!.info.length })

                queueContents.forEach { audioContent ->
                    val time = getTimestamp(audioContent.track!!.info.length)
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

                if (pages.size != 0) {
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
                var firstTrack = playlist.selectedTrack

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                if (playlist.name.contains(SEARCH_INDICATOR)) {
                    trackList.add(firstTrack)
                } else {
                    playlist.tracks.forEach { track ->
                        trackList.add(track)
                    }
                }
            }

            override fun noMatches() {
                channel.sendMessage("Nothing found in $search nora.").queue()
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
                    var firstTrack = playlist.selectedTrack

                    if (firstTrack == null) {
                        firstTrack = playlist.tracks[0]
                    }

                    if (playlist.name.contains(SEARCH_INDICATOR)) {
                        trackList.add(firstTrack)
                    } else {
                        playlist.tracks.forEach { track ->
                            trackList.add(track)
                        }
                    }
                }

                override fun noMatches() {
                    channel.sendMessage("Nothing found in $trackUrl nora.").queue()
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
        if (option.contains(YT_SEARCH)) {
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
        if (option.contains(YT_SEARCH))
            loadAudioTrackFromSearch(option).map {
                RequestedTrackInfo(
                    it,
                    event.member!!.user,
                    event.guild!!
                )
            }
        else {
            loadAudioTracks(option.split(" ")).map {
                RequestedTrackInfo(
                    it,
                    event.member!!.user,
                    event.guild!!
                )
            }
        }

    companion object {
        private const val SEARCH_INDICATOR = "Search results for:"
        private const val ENTRY_LIMIT = 10
        private const val YT_SEARCH = "ytsearch:"
    }
}