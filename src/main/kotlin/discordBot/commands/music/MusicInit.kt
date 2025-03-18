package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager


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

private const val notInVC = "Entra no chat de voz com os manos!"

/**
 * This function acts like a module
 * Load it somewhere, get their returning value and place inside `jda.updateCommands().addCommand()`
 * to queue it and this entire package will start working~
 */
fun musicInit(jda: JDA): SlashCommandData {
    val musicManagers = mutableMapOf<Long, GuildMusicManager>()
    val playerManager = DefaultAudioPlayerManager()
    // creating new audio source manager from dev.lavalink.youtube.YoutubeAudioSourceManager
    val ytSourceManager = dev.lavalink.youtube.YoutubeAudioSourceManager()
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

    fun getGuildAudioPlayer(guild: Guild, channel: MessageChannel): GuildMusicManager {
        val guildId = guild.id.toLong()
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager, channel)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()

        return musicManager
    }

    jda.onCommand(MusicCommands.MUSIC) { event ->
        if (!event.isFromGuild) {
            event.reply("Você nem está num servidor!").queue()
            return@onCommand
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        if (isSameVC(event) || guildAudioPlayer.player.playingTrack == null) {
            Music(guildAudioPlayer, playerManager).tryExecute(event)
        } else {
            event.reply(notInVC).setEphemeral(true).queue()
        }
    }

    jda.onButton(MusicCommands.STOP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.player.stopTrack()
        if (event.guild!!.audioManager.isConnected) {
            event.guild!!.audioManager.closeAudioConnection()
        }
        musicManagers.remove(event.guild!!.id.toLong())
        event.reply(event.user.name + " me mandou parar~").queue()
    }

    jda.onButton(MusicCommands.PLAY) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.player.isPaused = !guildAudioPlayer.player.isPaused
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(MusicCommands.SKIP) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        event.reply("Pulando...").queue()
        guildAudioPlayer.scheduler.nextTrack(true)
    }

    jda.onButton(MusicCommands.REPEAT) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.toggleRepeat()
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    jda.onButton(MusicCommands.SHUFFLE) { event ->
        if (!isSameVC(event)) {
            event.reply(notInVC).setEphemeral(true).queue()
            return@onButton
        }

        val guildAudioPlayer = getGuildAudioPlayer(event.guild!!, event.messageChannel)
        guildAudioPlayer.scheduler.shuffle(true)
        event.editMessageEmbeds(listOf(MusicInfoEmbed.getUpdatedEmbed(event, guildAudioPlayer))).queue()
    }

    return MusicCommands.getCommands()
}
