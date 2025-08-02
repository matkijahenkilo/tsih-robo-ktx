package discordBot.hybridCommands.birthday

import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.JDA
import org.matkija.bot.discordBot.abstracts.TimedEvent
import org.matkija.bot.sql.JPAUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.util.*

class BirthdayMessageSenderTimedEvent(private val jda: JDA) : TimedEvent() {

    private val logger = LoggerFactory.getLogger(BirthdayMessageSenderTimedEvent::class.java.name)
    private val messages = listOf(
        "🎉 It's %s's birthday nanora! Happy birthday!! ❤️",
        "🎉 It's %s's birthday nanora! Let's eat some cake!!🍰 ❤️",
        "Let's celebrate nanora! It's %s's birthday!! ❤️",
        "It's %s's birthday nora! Let's celebrate nanora!! ❤️",
        "%s sounds a lot older today... Happy birthday nora!! ❤️",
    )

    private val stateFile = File("data/birthday_state.properties")

    private fun getLastSentDay(): String {
        if (!stateFile.exists()) return ""
        val props = Properties()
        stateFile.inputStream().use { props.load(it) }
        return props.getProperty("lastSentDate", "")
    }

    private fun markDayAsSent(dateString: String) {
        val props = Properties()
        props.setProperty("lastSentDate", dateString)
        stateFile.outputStream().use { props.store(it, "Last birthday notification sync") }
    }

    override val task: Runnable = Runnable {
        val now = LocalDateTime.now()
        val thisDay = now.dayOfMonth.toLong()
        val thisMonthNumber = now.month.value.toLong()
        val todayString = "${now.year}-${now.monthValue}-${now.dayOfMonth}"

        logger.info("Checking for birthdays...")

        if (getLastSentDay() != todayString) {

            val birthdayUsers = JPAUtil.getTodayBirthdays(thisDay, thisMonthNumber)

            if (birthdayUsers.isNullOrEmpty()) {
                markDayAsSent(todayString)
                logger.info("List of birthday is empty or null, not running")
                return@Runnable
            }

            birthdayUsers.forEach { birthdaySubscription ->
                if (thisDay == birthdaySubscription.birthdayUserId.day && thisMonthNumber == birthdaySubscription.birthdayUserId.month) {
                    val user = jda.retrieveUserById(birthdaySubscription.birthdayUserId.userId).complete()
                    val channel = jda.getTextChannelById(birthdaySubscription.birthdayChannelId.channelId)
                    channel?.send(
                        content = messages.random().format(user.asMention),
                    )!!.queue()
                    logger.info("Sent congrats for ${user.name} in #${channel.name} from ${channel.guild.name}")
                }
            }

            markDayAsSent(todayString)

            logger.info("Finished checking for birthdays")
        } else {
            logger.info("Already checked for today")
        }
    }
}