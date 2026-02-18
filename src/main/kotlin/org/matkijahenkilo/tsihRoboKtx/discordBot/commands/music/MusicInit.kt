package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.*
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.LOG
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.audio.GuildMusicManager
import org.matkijahenkilo.tsihRoboKtx.utils.clearCRLF
import java.io.File
import dev.lavalink.youtube.clients.Music as lavalinkMusic


fun musicInit(jda: JDA): SlashCommandData {

    val musicManagers = mutableMapOf<Long, GuildMusicManager>()
    val playerManager = DefaultAudioPlayerManager()
    val notInVC = "You're not in a vc, nora!"
    // creating new audio source manager from dev.lavalink.youtube.YoutubeAudioSourceManager
    val ytSourceManager = YoutubeAudioSourceManager(
        true,
        Web(),
        lavalinkMusic(),
        MWeb(),
        WebEmbedded(),
        AndroidMusic(),
        AndroidVr(),
        Ios(),
        Tv(),
        TvHtml5Embedded()
    )
    // registering it into playerManager
    playerManager.registerSourceManager(ytSourceManager)
    // excluding deprecated audioSourceManager
    @Suppress("DEPRECATION") // watch yo tone, Machine
    AudioSourceManagers.registerRemoteSources(
        playerManager,
        com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java
    )

    AudioSourceManagers.registerRemoteSources(playerManager)
    AudioSourceManagers.registerLocalSource(playerManager)

    val oauthFile = File("data/oauth.txt")
    if (oauthFile.exists()) {
        val refreshToken = oauthFile.readText().clearCRLF()
        ytSourceManager.useOauth2(refreshToken, true)
    } else {
        LOG.warn(
            "HOIST UP THE SAILS LOOK OUT YOU LANDLUBBERS, " +
                    "It is recommended to use oauth in order to use services such as YouTube. " +
                    "Restart the program with -t argument"
        )
    }

    fun getGuildAudioPlayer(guild: Guild, channel: MessageChannel?): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager, channel!!)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()

        return musicManager
    }

    fun isSameVC(event: GenericCommandInteractionEvent): Boolean =
        if (event.guild!!.audioManager.connectedChannel != null) {
            event.member!!.voiceState!!.channel!!.id == event.guild!!.audioManager.connectedChannel!!.id
        } else {
            false
        }

    fun isSameVC(event: ButtonInteractionEvent): Boolean =
        if (event.guild!!.audioManager.connectedChannel != null) {
            event.member!!.voiceState!!.channel!!.id == event.guild!!.audioManager.connectedChannel!!.id
        } else {
            false
        }

    fun stopAndCloseConnection(event: GenericGuildEvent) {
        val guildAudioPlayer = getGuildAudioPlayer(event.guild, null)
        guildAudioPlayer.player.stopTrack()
        if (event.guild.audioManager.isConnected) event.guild.audioManager.closeAudioConnection()
        musicManagers.remove(event.guild.idLong)
    }

    fun stopAndCloseConnection(event: ButtonInteractionEvent) {
        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, null)
        guildAudioPlayer.player.stopTrack()
        if (event.guild!!.audioManager.isConnected) event.guild!!.audioManager.closeAudioConnection()
        musicManagers.remove(event.guild!!.idLong)
    }

    jda.listener<GuildVoiceUpdateEvent> { event ->
        val leftChannel: AudioChannelUnion? = event.channelLeft

        // check if the event was a `channelLeft` and if there's only one member in voice channel
        if (leftChannel != null && leftChannel.members.size == 1) {
            // checks if last member in audio channel is this jda instance
            val lastMemberInVc = leftChannel.members.find { it.user == jda.selfUser }
            if (lastMemberInVc != null) stopAndCloseConnection(event)
        }
    }

    jda.onCommand(Music.MUSIC) { event ->
        if (!event.isFromGuild) {
            event.reply("You're not even in a server nanora!").queue()
            return@onCommand
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        if (isSameVC(event) || guildAudioPlayer.player.playingTrack == null) {
            Music(event, guildAudioPlayer, playerManager).tryExecute()
        } else {
            event.reply(notInVC).setEphemeral(true).queue()
        }
    }

    jda.onButton(Music.STOP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        stopAndCloseConnection(event)
        event.reply(
            event.user.name + " told me to stop~\n" +
                    "-# if I was playing something, I can recover the playlist with `/${Music.MUSIC} ${Music.MUSIC_RESUME_QUEUE}` nanora!"
        ).queue()
    }

    jda.onButton(Music.PLAY) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.player.isPaused = !guildAudioPlayer.player.isPaused
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(Music.SKIP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        event.reply(event.user.name + " skipped, nora...").queue()
        guildAudioPlayer.scheduler.nextTrack(true)
    }

    jda.onButton(Music.REPEAT) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.toggleRepeat()
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(Music.SHUFFLE) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.shuffle(true)
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    return Music.getCommands()
}
