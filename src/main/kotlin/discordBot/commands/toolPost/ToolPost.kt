package discordBot.commands.toolPost

import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random


class ToolPost : SlashCommand() {

    companion object {
        val POG: Logger = LoggerFactory.getLogger(ToolPost::class.java)
    }

    override fun execute(event: GenericCommandInteractionEvent) {
        if (baseVideoExists()) {

            val link = event.getOption(ToolPostOptions.TOOLPOST_OPTION_LINK)!!.asString

            if (!link.contains("https://")) {
                event.reply("This is not a valid link nanora!").setEphemeral(true).queue()
                return
            }

            event.deferReply().queue()

            val timeToTrim = event.getOption(ToolPostOptions.TOOLPOST_OPTION_TRIM)?.asDouble
            val output = createToolPost(link, timeToTrim ?: Random.nextDouble(10.0, 50.0))

            if (output != null) {
                val video = FileUpload.fromData(output)
                event.hook.editMessage(content = "Created toolpost from <$link>").queue()
                event.messageChannel.send(files = listOf(video)).queue()
            } else {
                event.hook.editMessage(content = "Failed to fetch song from link nanora...").queue()
            }
        } else {
            val ownerName: String = event.jda.retrieveApplicationInfo().complete().owner.name
            event.reply("An important file is missing from the hosts' computer, annoy the HECK out of $ownerName nanora!")
                .queue()
        }
    }

    private fun createToolPost(link: String, timeToTrim: Double): File? {
        val audioFile = downloadAudio(link)
        if (audioFile != null) {
            val trimmedAudio = trimAudio(audioFile, timeToTrim)
            val output = mergeAudioWithVideo(trimmedAudio)
            File("$PATH/$trimmedAudio").delete()
            return output
        } else {
            POG.warn("Audio file returned null from $link")
            return null
        }
    }
}