package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.lavalink.youtube.clients.*
import dev.lavalink.youtube.clients.Music
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.LOG
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import java.io.File


private fun isSameVC(event: GenericCommandInteractionEvent): Boolean =
    if (event.guild!!.audioManager.connectedChannel != null) {
        event.member!!.voiceState!!.channel!!.id == event.guild!!.audioManager.connectedChannel!!.id
    } else {
        false
    }

private fun isSameVC(event: ButtonInteractionEvent): Boolean =
    if (event.guild!!.audioManager.connectedChannel != null) {
        event.member!!.voiceState!!.channel!!.id == event.guild!!.audioManager.connectedChannel!!.id
    } else {
        false
    }

private const val notInVC = "You're not in a vc, nora!"

fun musicInit(jda: JDA): SlashCommandData {

    val musicManagers = mutableMapOf<Long, GuildMusicManager>()
    val playerManager = DefaultAudioPlayerManager()
    // creating new audio source manager from dev.lavalink.youtube.YoutubeAudioSourceManager
    val ytSourceManager = dev.lavalink.youtube.YoutubeAudioSourceManager(
        true,
        Web(),
        Music(),
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
        val refreshToken = oauthFile.readText()
        ytSourceManager.useOauth2(refreshToken, true)
    } else {
        LOG.warn("HOIST UP THE SAILS LOOK OUT YOU LANDLUBBERS, It is recommended to use oauth in order to use services such as YouTube. Restart the program with -t argument")
    }

    fun getGuildAudioPlayer(guild: Guild, channel: MessageChannel): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager, channel)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()

        return musicManager
    }

    jda.onCommand(MusicSlashCommands.MUSIC) { event ->
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

    jda.onButton(MusicSlashCommands.STOP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.player.stopTrack()
        if (event.guild!!.audioManager.isConnected) {
            event.guild!!.audioManager.closeAudioConnection()
        }
        musicManagers.remove(event.guild!!.idLong)
        event.reply(event.user.name + " told me to stop, nora~").queue()
    }

    jda.onButton(MusicSlashCommands.PLAY) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.player.isPaused = !guildAudioPlayer.player.isPaused
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(MusicSlashCommands.SKIP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        event.reply(event.user.name + " skipped, nora...").queue()
        guildAudioPlayer.scheduler.nextTrack(true)
    }

    jda.onButton(MusicSlashCommands.REPEAT) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.toggleRepeat()
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(MusicSlashCommands.SHUFFLE) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.shuffle(true)
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    return MusicSlashCommands.getCommands()
}
