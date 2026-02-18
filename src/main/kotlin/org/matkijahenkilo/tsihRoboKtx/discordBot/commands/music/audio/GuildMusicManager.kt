package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

/**
 * Holder for the player, a track scheduler and a channel to send messages for one guild.
 *
 * Creates a player and a track scheduler.
 * @param manager Audio player manager to use for creating the player.
 */
class GuildMusicManager(manager: AudioPlayerManager, channel: MessageChannel) {
    /**
     * Audio player for the guild.
     */
    val player: AudioPlayer = manager.createPlayer()

    /**
     * Track scheduler for the player.
     */
    val scheduler: TrackScheduler = TrackScheduler(player, channel)

    init {
        player.addListener(scheduler)
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)
}