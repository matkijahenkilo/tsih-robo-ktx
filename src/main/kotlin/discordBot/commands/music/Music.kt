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
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import org.matkija.bot.discordBot.utils.getTimestamp
import org.matkija.bot.utils.TsihPoggers
import kotlin.time.Duration.Companion.minutes

class Music(
    private val musicManager: GuildMusicManager,
    private val playerManager: AudioPlayerManager
) : SlashCommand() {

    //TODO: make it work in many servers at the same time
    private val playlistJsonHandler = PlaylistJsonHandler("data/playlist.json")

    override fun execute(event: GenericCommandInteractionEvent) {
        event.deferReply().queue()

        var option = event.getOption(MusicCommands.MUSIC_OPTION_LINK)?.asString
        val originalOption = option
        val website = event.getOption(MusicCommands.MUSIC_OPTION_SEARCH)?.asString

        if (option != null) {
            if (website != null) {
                option = website + option
            } else if (!option.contains("https://")) {
                option = "$YT_SEARCH$option"
            }
        }

        when (event.subcommandName) {
            MusicCommands.MUSIC_PLAY -> {
                if (option != null) {
                    event.hook.editMessage(content = "Carregando música(s)...")
                        .queue()
                    loadAndPlay(event, option, null)
                    if (option.contains(YT_SEARCH)) {
                        event.hook.editMessage(content = "Carreguei o primeiro resultado de `$originalOption`!")
                            .queue()
                    } else {
                        event.hook.editMessage(content = "Carreguei a [música ou playlist](<${option}>)!")
                            .queue()
                    }
                }
            }

            MusicCommands.MUSIC_RESUME_TRACK_LIST -> {
                if (musicManager.player.playingTrack != null) {
                    event.hook.editMessage(
                        content = "Uma playlist já está sendo tocada."
                    ).queue()
                    return
                }

                var loaded = false

                event.hook.editMessage(content = "Resumindo playlist anteriormente salva...")
                    .queue()

                playlistJsonHandler.getPlaylist().forEach {
                    val requester = event.jda.retrieveUserById(it.requester).complete()
                    loadAndPlay(event, it.link, requester)
                    loaded = true
                }
                if (!loaded) {
                    event.hook.editMessage(
                        content = "A playlist salva está vazia! Use `/${MusicCommands.MUSIC} ${MusicCommands.MUSIC_PLAY}` para começar uma nova playlist~"
                    ).queue()
                } else {
                    event.hook.editMessage(content = "Retomei a playlist salva!")
                        .queue()
                }
            }

            MusicCommands.MUSIC_SHOW_TRACK_LIST -> {
                val originalQueueContents = musicManager.scheduler.originalQueue
                val pages = mutableSetOf<MessageEmbed>()
                var content = mutableListOf<String>()
                var index = 1
                var totalLength: Long = 0

                originalQueueContents.forEach { totalLength += it.track.info.length }
                val totalTime = getTimestamp(totalLength)

                originalQueueContents.forEach { audioContent ->
                    val time = getTimestamp(audioContent.track.info.length)
                    content.add(
                        String.format(
                            "%s. [%s](%s) (%s) por %s\n",
                            index,
                            audioContent.track.info.title,
                            audioContent.track.info.uri,
                            time,
                            audioContent.requester.name
                        )
                    )
                    if (content.size == ENTRY_LIMIT || index == originalQueueContents.size) {
                        var str = ""
                        content.forEach { str += it }
                        var embed = EmbedBuilder {
                            title = "As próximas a serem tocadas..."
                            description = str
                            color = 0xff80fd
                            footer {
                                name = "Duração total desta track list: $totalTime"
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
                    event.hook.editMessage(content = "Nada na playlist!").queue()
                }
            }
        }
    }

    private fun loadAndPlay(event: GenericCommandInteractionEvent, trackUrl: String, oldRequester: User?) {
        val channel = event.messageChannel
        val guild = event.guild!!

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                play(guild, musicManager, event.member!!, track, oldRequester)

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
                    play(guild, musicManager, event.member!!, firstTrack, oldRequester)
                } else {
                    playlist.tracks.forEach { track ->
                        play(guild, musicManager, event.member!!, track, oldRequester)
                    }
                }

                if (musicManager.scheduler.isShuffled) {
                    musicManager.scheduler.shuffle(false)
                }
            }

            override fun noMatches() {
                channel.sendMessage("Nada encontrado de $trackUrl").queue()
            }

            override fun loadFailed(exception: FriendlyException) {
                TsihPoggers.POG.error(exception.stackTraceToString())
                channel.sendMessage("Não pude tocar nada... razão: ${exception.message}")
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
        oldRequester: User?
    ) {
        connectToMemberVC(guild.audioManager, member)
        musicManager.scheduler.queue(member, track, oldRequester)
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