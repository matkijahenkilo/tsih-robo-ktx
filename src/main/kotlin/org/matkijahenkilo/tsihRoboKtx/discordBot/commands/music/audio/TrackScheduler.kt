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


class TrackScheduler(
    private val player: AudioPlayer, private val channel: MessageChannel
) : AudioEventAdapter(), TrackListEventInterface {

    // priority over currentQueue
    val priorityQueue: MutableList<RequestedTrackInfo> = mutableListOf()

    // this is what plays on the bot, is also used for the SHOW_QUEUE command, and it's content
    // are saved on disk
    var currentQueue: MutableList<RequestedTrackInfo> = mutableListOf()

    // used to repeat the audio without deleting it from the list
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
                        priorityQueue.add(entry)
                    } else {
                        currentQueue.add(entry)
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
     * @param skip if scheduler should skip the current track for the next one in the list
     */
    fun nextTrack(skip: Boolean) {
        if (isRepeating && !skip) {
            lastContent =
                RequestedTrackInfo(lastContent!!.track!!.makeClone(), lastContent!!.requester, lastContent!!.guild)
            player.startTrack(lastContent!!.track, true)
        } else {
            var content: RequestedTrackInfo?

            if (priorityQueue.isNotEmpty())
                content = priorityQueue.removeFirstOrNull()
            else {
                if (isShuffled) {
                    content = currentQueue.randomOrNull()
                    currentQueue.remove(content)
                } else
                    content = currentQueue.removeFirstOrNull()
            }
            if (content == null) { // if there's nothing left to play in track list
                player.startTrack(null, false)
            } else {
                lastContent = RequestedTrackInfo(content.track!!.makeClone(), content.requester, content.guild)
                player.startTrack(content.track, false)
                MusicInfoEmbed.postEmbed(content, channel, this, player)
            }
        }
    }

    fun toggleShuffle() {
        isShuffled = !this.isShuffled
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