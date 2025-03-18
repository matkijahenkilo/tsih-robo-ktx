package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.User

data class AudioContent(
    val track: AudioTrack,
    val requester: User
)