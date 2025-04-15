package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.matkija.bot.LOG
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import kotlin.time.Duration.Companion.minutes

class Music(
    private val musicManager: GuildMusicManager,
    private val playerManager: AudioPlayerManager
) : SlashCommand() {

    //TODO: make it work in many servers at the same time
    private val playlistJsonHandler = PlaylistJsonHandler("data/playlist.json")

    override fun execute(event: GenericCommandInteractionEvent) {
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
                    event.hook.editMessage(content = "Loading song(s)...")
                        .queue()
                    loadAndPlay(event, option, null)
                    if (option.contains(YT_SEARCH)) {
                        event.hook.editMessage(content = "Loaded the first song of result `$originalOption` nanora!")
                            .queue()
                    } else {
                        event.hook.editMessage(content = "Loaded a [song or playlist](<${option}>) nanora!")
                            .queue()
                    }
                }
            }

            MusicSlashCommands.MUSIC_PLAY_NEXT -> {
                if (option != null) {
                    event.hook.editMessage(content = "Loading song(s)...")
                        .queue()
                    loadAndPlay(event, option, null, true)
                    if (option.contains(YT_SEARCH)) {
                        event.hook.editMessage(content = "Loaded the first song of result `$originalOption` nanora!")
                            .queue()
                    } else {
                        event.hook.editMessage(content = "Loaded a [song or playlist](<${option}>) nanora!")
                            .queue()
                    }
                }
            }

            MusicSlashCommands.MUSIC_RESUME_QUEUE -> {
                if (musicManager.player.playingTrack != null) {
                    event.hook.editMessage(
                        content = "A playlist is already being played nanora."
                    ).queue()
                    return
                }

                var loaded = false

                event.hook.editMessage(content = "Resuming a saved playlist nora...")
                    .queue()

                playlistJsonHandler.getPlaylist().forEach {
                    val requester = event.jda.retrieveUserById(it.requester).complete()
                    loadAndPlay(event, it.link, requester)
                    loaded = true
                }
                if (!loaded) {
                    event.hook.editMessage(
                        content = "There's no playlist saved nanora! Use `/${MusicSlashCommands.MUSIC} ${MusicSlashCommands.MUSIC_PLAY}` to start a new one nora~"
                    ).queue()
                } else {
                    event.hook.editMessage(content = "Resumed the saved playlist nanora!")
                        .queue()
                }
            }

            MusicSlashCommands.MUSIC_SHOW_QUEUE -> {
                val queueContents = musicManager.scheduler.priorityQueue + musicManager.scheduler.originalQueue
                val pages = mutableSetOf<MessageEmbed>()
                var content = mutableListOf<String>()
                var index = 1

                val totalTime = getTimestamp(queueContents.sumOf { it.track.info.length })

                queueContents.forEach { audioContent ->
                    val time = getTimestamp(audioContent.track.info.length)
                    content.add(
                        String.format(
                            "%s. [%s](%s) (%s) by %s",
                            index,
                            audioContent.track.info.title,
                            audioContent.track.info.uri,
                            time,
                            audioContent.requester.name
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

    private fun loadAndPlay(
        event: GenericCommandInteractionEvent,
        trackUrl: String,
        oldRequester: User?,
        isPriority: Boolean = false
    ) {
        val channel = event.messageChannel
        val guild = event.guild!!

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                play(guild, musicManager, event.member!!, track, oldRequester, isPriority)

                if (musicManager.scheduler.isShuffled) {
                    musicManager.scheduler.shuffle(false)
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                var firstTrack = playlist.selectedTrack

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                if (playlist.name.contains(SEARCH_INDICATOR)) {
                    play(guild, musicManager, event.member!!, firstTrack, oldRequester, isPriority)
                } else {
                    playlist.tracks.forEach { track ->
                        play(guild, musicManager, event.member!!, track, oldRequester, isPriority)
                    }
                }

                if (musicManager.scheduler.isShuffled) {
                    musicManager.scheduler.shuffle(false)
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

    /**
     * Plays or queue an AudioTrack
     */
    private fun play(
        guild: Guild,
        musicManager: GuildMusicManager,
        member: Member,
        track: AudioTrack,
        oldRequester: User?,
        isPriority: Boolean
    ) {
        connectToMemberVC(guild.audioManager, member)
        musicManager.scheduler.queue(member, track, oldRequester, isPriority)
    }

    private fun connectToMemberVC(audioManager: AudioManager, member: Member) {
        if (!audioManager.isConnected/* && !audioManager.isAttemptingToConnect()*/) {
            audioManager.openAudioConnection(member.voiceState!!.channel)
        }
    }

    companion object {
        private const val SEARCH_INDICATOR = "Search results for:"
        private const val ENTRY_LIMIT = 10
        private const val YT_SEARCH = "ytsearch:"
    }
}