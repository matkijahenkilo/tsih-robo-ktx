package org.matkija.bot.discordBot.abstracts

import org.matkija.bot.utils.TsihPoggers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class TimedEvent {

    private val scheduler = Executors.newScheduledThreadPool(1)

    /**
     * Example implementation:
     * ```
     * override val task: Runnable = Runnable {
     *     // Check if it is 6 PM (18:00) on the current day.
     *     if (LocalTime.now().hour == 18 && LocalTime.now().minute == 0) {
     *         println("It's 6 PM, executing the function!")
     *         // Call your function here
     *         myFunction()
     *     }
     * }
     * ```
     */
    protected abstract val task: Runnable

    fun startScheduler(every: TimeUnit, initialDelay: Long, period: Long): Any = try {
        scheduler.scheduleAtFixedRate(task, initialDelay, period, every)
    } catch (e: Exception) {
        TsihPoggers.POG.error(e.toString())
    }
}