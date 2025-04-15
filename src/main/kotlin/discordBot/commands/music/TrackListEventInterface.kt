package org.matkija.bot.discordBot.commands.music

interface TrackListEventInterface {
    fun saveTracks(requestedTrackInfos: List<RequestedTrackInfo>)
    fun deleteTrack(link: String, guildId: Long)
}