package org.matkija.bot.discordBot.commands.tsihOClockTimer

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.FileUpload
import org.matkija.bot.discordBot.abstracts.TimedEvent
import org.matkija.bot.sql.JPAUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant

class TsihOClockTimer(private val jda: JDA) : TimedEvent() {

    private val pog: Logger = LoggerFactory.getLogger(TsihOClockTimer::class.java)

    private val footerImage = "sauce-footer.png"
    private var footerImagePath: MutableList<FileUpload>? = null

    init {
        val footerImage = File("data/images/$footerImage")
        if (footerImage.exists())
            footerImagePath = mutableListOf(FileUpload.fromData(footerImage))
    }

    override val task: Runnable = Runnable {
        CoroutineScope(Dispatchers.IO).launch {
            val channelList = JPAUtil.getAllTsihOClockRooms()

            if (channelList.isNotEmpty()) {
                // TODO: TOC counter
                val files = File("data/images/tsihoclock").listFiles()!!
                val jobList = mutableListOf<Job>()
                pog.info("Sending images to ${channelList.size} channels")
                channelList.forEach { obj ->
                    jobList += async {
                        val channel = jda.getTextChannelById(obj.channelId)
                        val file = files.random()
                        val fileUpload = listOf(FileUpload.fromData(file))
                        var fileUploadWithFooterImage = fileUpload
                        if (footerImagePath != null)
                            fileUploadWithFooterImage = footerImagePath!! + fileUpload

                        if (channel != null) {
                            channel.send(
                                files = fileUploadWithFooterImage,
                                embeds = listOf(
                                    EmbedBuilder {
                                        title = randomTitle()
                                        color = 0xff80fd
                                        field {
                                            name = randomName()
                                            value = randomValue()
                                        }
                                        image = "attachment://${file.name}"
                                        footer {
                                            name = "owo"
                                            iconUrl = "attachment://$footerImage"
                                        }
                                        timestamp = Instant.now()
                                    }.build()
                                )
                            ).queue()
                        } else {
                            pog.warn("Id $obj doesn't exist")
                        }
                    }
                }
                jobList.joinAll()
            }
        }
    }
}