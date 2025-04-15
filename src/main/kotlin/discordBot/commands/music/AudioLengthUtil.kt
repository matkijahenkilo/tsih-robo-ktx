package org.matkija.bot.discordBot.commands.music

import java.util.concurrent.TimeUnit

fun getTimestamp(ms: Long): String {
    val songLength = getTranslatedSongLength(ms)
    return if (songLength.hour.toInt() < 1) {
        String.format("%s:%s", songLength.minute, songLength.second)
    } else {
        String.format("%s:%s:%s", songLength.hour, songLength.minute, songLength.second)
    }
}

private fun getTranslatedSongLength(ms: Long): SongLength {
    val h: Long = TimeUnit.MILLISECONDS.toHours(ms)
    val rawMinute: Long = TimeUnit.MILLISECONDS.toMinutes(ms)
    val m: Long = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(h)
    val s: Long = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(rawMinute)

    val correctedHour: String = if (h < 10) "0$h" else h.toString()
    val correctedMinute: String = if (m < 10) "0$m" else m.toString()
    val correctedSecond: String = if (s < 10) "0$s" else s.toString()

    return SongLength(correctedHour, correctedMinute, correctedSecond)
}