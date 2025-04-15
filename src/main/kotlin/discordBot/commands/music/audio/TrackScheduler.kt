package org.matkija.bot.discordBot.commands.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
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

    // priority over currentQueue
    val priorityQueue: BlockingQueue<AudioContent> = LinkedBlockingQueue()

    // this is what plays on the bot, can be shuffled and unshuffled
    private var currentQueue: BlockingQueue<AudioContent> = LinkedBlockingQueue()

    // used to keep track of the entire track and to be saved to disk
    val originalQueue: BlockingQueue<AudioContent> = LinkedBlockingQueue()

    // used to repeat the audio without deleting it from another queue with .poll()
    private var lastContent: AudioContent? = null

    private val playlistJsonHandler: PlaylistJsonHandler = PlaylistJsonHandler("data/playlist.json")
    var isShuffled: Boolean = false
    var isRepeating: Boolean = false

    /**
     * Add the next track to queue or play right away if nothing is in the current queue.
     *
     * @param member The user to show who requested it.
     * @param track The track to play or add to queue.
     * @param oldRequester The previous requester from the playlist.json, if available.
     * @param isPriority If the track should be played next regardless of the queue size
     */
    fun queue(member: Member, track: AudioTrack, oldRequester: User?, isPriority: Boolean) {
        val content = if (oldRequester == null) {
            AudioContent(track, member.user)
        } else {
            AudioContent(track, oldRequester)
        }

        if (!player.startTrack(content.track, true)) {
            if (isPriority) {
                priorityQueue.offer(content)
            } else {
                currentQueue.offer(content)
                originalQueue.offer(content)
            }
            savePlaylist()
        } else {
            lastContent = AudioContent(content.track.makeClone(), content.requester)
            MusicInfoEmbed.postEmbed(content, channel, this, player)
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     *
     * @param skip if scheduler should skip the current track for the next one in BlockingQueue
     */
    fun nextTrack(skip: Boolean) {
        if (isRepeating && !skip) {
            lastContent = AudioContent(lastContent!!.track.makeClone(), lastContent!!.requester)
            player.startTrack(lastContent!!.track, true)
        } else {
            val content = priorityQueue.poll() ?: currentQueue.poll()

            lastContent = AudioContent(content.track.makeClone(), content.requester)
            player.startTrack(content.track, false)
            originalQueue.remove(content)
            MusicInfoEmbed.postEmbed(content!!, channel, this, player)

            savePlaylist()
        }
    }

    private fun savePlaylist() {
        val list = (priorityQueue + originalQueue).map {
            PlaylistJsonHandler.SongEntry(it.track.info.uri, it.requester.id.toLong())
        }
        playlistJsonHandler.setPlaylist(list)
    }

    /**
     * Shuffles the playlist and save it to the current queue.
     * If undoing the shuffle, the original queue before shuffling will be the new playlist again with the removed tracks.
     *
     * @param toggleShuffle if the scheduler should enable/disable shuffling. Passing `true` will invert its current
     * status while passing `false` won't change its status. Regardless of the param received,
     * if `isShuffled` is currently `true`, the playlist will be shuffled
     */
    fun shuffle(toggleShuffle: Boolean) {
        if (toggleShuffle)
            isShuffled = !this.isShuffled

        currentQueue = if (isShuffled) {
            LinkedBlockingQueue(currentQueue.shuffled())
        } else {
            originalQueue
        }
    }

    fun toggleRepeat() {
        isRepeating = !this.isRepeating
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack(false)
        }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {}
}