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
        private const val TOOLPOST_DROP = 8 // climax of the video is at 8 seconds mark
    }

    override fun execute(event: GenericCommandInteractionEvent) {
        if (baseVideoExists()) {

            val link = event.getOption(ToolPostOptions.TOOLPOST_OPTION_LINK)!!.asString

            if (!link.contains("https://")) {
                event.reply("This is not a valid link nanora!").setEphemeral(true).queue()
                return
            }

            val fileDuration = getFileDuration(link)

            if (fileDuration < 16) {
                event.reply("This song is too short nanora!").setEphemeral(true).queue()
                return
            } else if (fileDuration >= 900) {
                event.reply("This song is too long nanora!").setEphemeral(true).queue()
                return
            }

            event.deferReply().queue()

            // it's hard for the user to input the length in seconds
            // what if the song's climax is at 3:46? it's annoying to calculate this time into seconds
            // maybe something good todo is to use the opposite of org.matkija.bot.discordBot.commands.music.AudioLengthUtil
            var timeToTrim = event.getOption(ToolPostOptions.TOOLPOST_OPTION_TRIM)?.asDouble
            if (timeToTrim != null && timeToTrim >= TOOLPOST_DROP) {
                if (timeToTrim >= fileDuration)
                    timeToTrim = null
                else
                    timeToTrim -= TOOLPOST_DROP
            }

            val output = createToolPost(link, timeToTrim = timeToTrim ?: Random.nextDouble(0.0, fileDuration))

            if (output != null) {
                val video = FileUpload.fromData(output)
                event.hook.editMessage(content = "Created toolpost from <$link>").queue()
                event.messageChannel.send(files = listOf(video)).queue()
            } else {
                event.hook.editMessage(content = "Failed to fetch song from link, maybe I'm not old enough to what this video nanora~")
                    .queue()
            }
        } else {
            val ownerName: String = event.jda.retrieveApplicationInfo().complete().owner.name
            POG.error("original.mp4 is missing from data/toolpost/")
            event.reply("An important file is missing from the hosts' computer, annoy the HECK out of $ownerName nanora!")
                .setEphemeral(true)
                .queue()
        }
    }

    private fun createToolPost(link: String, timeToTrim: Double): File? {
        val content = downloadContent(link)
        if (content != null) {
            val trimmedContent = trimContent(content, timeToTrim)
            val output = mergeContentWithVideo(trimmedContent)
            File("$PATH/$trimmedContent").delete()
            return output
        } else {
            POG.error("Audio file returned null from $link")
            return null
        }
    }
}