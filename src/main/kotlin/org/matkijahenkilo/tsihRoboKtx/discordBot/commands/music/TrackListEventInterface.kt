package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.music

interface TrackListEventInterface {
    fun saveTracks(requestedTrackInfos: List<RequestedTrackInfo>)
    fun deleteTrack(link: String, guildId: Long)
}