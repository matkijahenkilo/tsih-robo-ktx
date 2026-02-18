package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * This is a wrapper around AudioPlayer which makes it behave as an AudioSendHandler for JDA. As JDA calls canProvide
 * before every call to provide20MsAudio(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide20MsAudio().
 *
 * @param audioPlayer Audio player to wrap.
 */
class AudioPlayerSendHandler(private var audioPlayer: AudioPlayer) : AudioSendHandler {

    private var buffer: ByteBuffer = ByteBuffer.allocate(1024)
    private var frame: MutableAudioFrame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    // returns true if audio was provided
    override fun canProvide(): Boolean = audioPlayer.provide(frame)

    override fun provide20MsAudio(): ByteBuffer {
        // flip to make it a read buffer
        (buffer as Buffer?)!!.flip()
        return buffer
    }

    override fun isOpus(): Boolean = true
}