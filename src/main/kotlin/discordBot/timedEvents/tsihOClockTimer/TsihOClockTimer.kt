package discordBot.timedEvents.tsihOClockTimer

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.FileUpload
import org.matkija.bot.discordBot.timedEvents.tsihOClockTimer.randomName
import org.matkija.bot.discordBot.timedEvents.tsihOClockTimer.randomTitle
import org.matkija.bot.discordBot.timedEvents.tsihOClockTimer.randomValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class TsihOClockTimer(private val jda: JDA) {

    private val pog: Logger = LoggerFactory.getLogger(TsihOClockTimer::class.java)

    // TODO("my stupid ass even forgot if `files` list will have the path or not")
    private val files = File("data/images/tsihoclock").listFiles()!!

    fun sendImages() = runBlocking {
        // TODO("db not yet implemented")
        val channelList = listOf("ids")
        // TODO("TOC counter")
        val count = 2
        val jobList = mutableListOf<Job>()
        channelList.forEach { channelId ->
            jobList += async {
                val channel = jda.getTextChannelById(channelId)
                val file = FileUpload.fromData(files.random())
                if (channel != null) {
                    channel.send(
                        files = listOf(file),
                        embeds = listOf(
                            EmbedBuilder {
                                title = randomTitle()
                                color = if (count % 2 == 0) 0xff80fd else 0x80fdff
                                field {
                                    name = randomName()
                                    value = randomValue()
                                }
                                image = "attachment://$file" // is there a path?
                            }.build()
                        )
                    ).queue()
                } else {
                    pog.warn("Id $channelId doesn't exist")
                }
            }
        }
        jobList.joinAll()
    }
}