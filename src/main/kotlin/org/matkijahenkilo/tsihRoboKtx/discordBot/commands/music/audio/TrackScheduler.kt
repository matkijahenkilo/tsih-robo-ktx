package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.MusicInfoEmbed
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.RequestedTrackInfo
import org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music.TrackListEventInterface
import org.matkijahenkilo.tsihRoboKtx.sql.JPAUtil
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class TrackScheduler(
    private val player: AudioPlayer, private val channel: MessageChannel
) : AudioEventAdapter(), TrackListEventInterface {

    // priority over currentQueue
    val priorityQueue: BlockingQueue<RequestedTrackInfo> = LinkedBlockingQueue()

    // this is what plays on the bot, can be shuffled and unshuffled
    private var currentQueue: BlockingQueue<RequestedTrackInfo> = LinkedBlockingQueue()

    // used to keep track of the entire track and to be saved to disk
    val originalQueue: BlockingQueue<RequestedTrackInfo> = LinkedBlockingQueue()

    // used to repeat the audio without deleting it from another queue with .poll()
    private var lastContent: RequestedTrackInfo? = null

    var isShuffled: Boolean = false
    var isRepeating: Boolean = false

    /**
     * Add the next track to queue or play right away if nothing is in the current queue.
     *
     * @param requestedTracksInfo List of information containing AudioTracks and the User who requested it
     * @param isPriority If the track should be played next regardless of the queue size
     * @param shouldSaveToDb Decides if the given list should also be saved to the database
     */
    fun queueSongs(requestedTracksInfo: List<RequestedTrackInfo>, isPriority: Boolean, shouldSaveToDb: Boolean) {
        requestedTracksInfo.forEach { entry ->
            if (entry.track != null) {
                if (!player.startTrack(entry.track, true)) {
                    if (isPriority) {
                        priorityQueue.offer(entry)
                    } else {
                        currentQueue.offer(entry)
                        originalQueue.offer(entry)
                    }
                } else {
                    lastContent = entry
                    MusicInfoEmbed.postEmbed(entry, channel, this, player)
                }
            }
        }

        if (shouldSaveToDb) saveTracks(requestedTracksInfo)
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     *
     * @param skip if scheduler should skip the current track for the next one in BlockingQueue
     */
    fun nextTrack(skip: Boolean) {
        if (isRepeating && !skip) {
            lastContent =
                RequestedTrackInfo(lastContent!!.track!!.makeClone(), lastContent!!.requester, lastContent!!.guild)
            player.startTrack(lastContent!!.track, true)
        } else {
            val content = priorityQueue.poll() ?: currentQueue.poll()

            if (content == null) { // if there's nothing left to play in track list
                player.startTrack(null, false)
            } else {
                lastContent = RequestedTrackInfo(content.track!!.makeClone(), content.requester, content.guild)
                player.startTrack(content.track, false)
                originalQueue.remove(content)
                MusicInfoEmbed.postEmbed(content, channel, this, player)
            }
        }
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
        if (toggleShuffle) isShuffled = !this.isShuffled

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
        if (endReason.mayStartNext)
            nextTrack(false)

        // this can delete duplicates, what to do?
        if (track != null) deleteTrack(track.info.uri, lastContent!!.guild!!.idLong)
    }

    override fun saveTracks(requestedTrackInfos: List<RequestedTrackInfo>) {
        JPAUtil.savePlaylistEntries(requestedTrackInfos)
    }

    override fun deleteTrack(link: String, guildId: Long) {
        JPAUtil.deletePlaylistByStringAndId(link, guildId)
    }
}