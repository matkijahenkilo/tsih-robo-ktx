package org.matkija.bot.utils

import java.util.concurrent.TimeUnit

/**
 * Parse string into total seconds
 *
 * e.g. "2:00" -> 120
 */
fun parseDurationToSeconds(time: String?): Double? {
    if (time == null) return null

    val parts = time.split(':')
    var totalSeconds = 0.0
    var multiplier = 1.0

    // Iterate backwards: Seconds (with decimals) -> Minutes -> Hours
    for (i in parts.size - 1 downTo 0) {
        val numberStr = parts[i].filter { it.isDigit() || it == '.' }
        val value = numberStr.toDoubleOrNull() ?: 0.0 // can handle 30 and 30.500

        totalSeconds += value * multiplier

        multiplier *= 60.0
    }

    return totalSeconds
}

/**
 * Converts milliseconds into a formatted string (H:MM:SS or M:SS)
 *
 * e.g. 61000 -> "1:01"
 *
 * 7200000 -> "1:00:00"
 */
fun formatMillis(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60

    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}