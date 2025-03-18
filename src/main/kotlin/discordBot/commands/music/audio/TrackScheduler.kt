package org.matkija.bot.discordBot.commands.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.matkija.bot.discordBot.commands.music.AudioContent
import org.matkija.bot.discordBot.commands.music.MusicInfoEmbed
import org.matkija.bot.discordBot.commands.music.PlaylistJsonHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class TrackScheduler(
    private val player: AudioPlayer,
    private val channel: MessageChannel
) : AudioEventAdapter() {

    private var queue: BlockingQueue<AudioContent> = LinkedBlockingQueue()
    val originalQueue: BlockingQueue<AudioContent> = LinkedBlockingQueue()
    private var lastContent: AudioContent? = null
    private val playlistJsonHandler = PlaylistJsonHandler("data/playlist.json")
    var isShuffled = false
    var isRepeating = false

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param member The user to show who requested it.
     * @param track The track to play or add to queue.
     * @param oldRequester The previous requester from the playlist.json, if available.
     */
    fun queue(member: Member, track: AudioTrack, oldRequester: User?) {
        val content = if (oldRequester == null) {
            AudioContent(track, member.user)
        } else {
            AudioContent(track, oldRequester)
        }

        if (!player.startTrack(content.track, true)) {
            queue.offer(content)
            originalQueue.offer(content)
            savePlaylist()
        } else {
            lastContent = AudioContent(content.track.makeClone(), content.requester)
            postEmbed(content)
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack(skip: Boolean) {
        if (isRepeating && !skip) {
            lastContent = AudioContent(lastContent!!.track.makeClone(), lastContent!!.requester)
            player.startTrack(lastContent!!.track, true)
        } else {
            val content = queue.poll()

            lastContent = AudioContent(content.track.makeClone(), content.requester)
            player.startTrack(content.track, false)
            originalQueue.remove(content)
            postEmbed(content!!)

            savePlaylist()
        }
    }

    private fun postEmbed(content: AudioContent) = channel.send(
        components = MusicInfoEmbed.loadComponents(),
        embeds = listOf(MusicInfoEmbed.loadPlayingEmbed(content.requester, content.track.info, this, player.isPaused))
    ).queue()

    private fun savePlaylist() {
        val list = mutableListOf<PlaylistJsonHandler.SongEntry>()
        originalQueue.forEach {
            list.add(PlaylistJsonHandler.SongEntry(it.track.info.uri, it.requester.id.toLong()))
        }
        playlistJsonHandler.setPlaylist(list)
    }

    /**
     * Shuffles the playlist and save it to the current queue.
     * If undoing the shuffle, the original queue before shuffling will be the new playlist again with the removed tracks.
     */
    fun shuffle(toggleShuffle: Boolean) {
        if (toggleShuffle)
            isShuffled = !isShuffled

        queue = if (isShuffled) {
            LinkedBlockingQueue(queue.shuffled())
        } else {
            originalQueue
        }
    }

    fun toggleRepeat() {
        isRepeating = !isRepeating
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack(false)
        }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}
}