package discordBot.commands.toolPost

import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.utils.clearCRLF
import org.matkija.bot.utils.parseDurationToSeconds
import org.matkija.bot.utils.replaceInstantChars
import org.matkija.bot.utils.replaceLast
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import kotlin.random.Random


class ToolPost(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ToolPost::class.java)
        private const val TOOLPOST_DROP = 8.633 // climax of the video is at 8 seconds and 633 milliseconds mark
        private const val PATH = "data/toolpost"
        private val FFMPEG_BASE_COMMAND = listOf("ffmpeg", "-y", "-i")
        const val ORIGINAL_VIDEO = "original.mp4"
        val toolPostWorkingDir = File(PATH)
    }

    override fun execute() {
        if (toolPostBaseVideoExists()) {

            val link = event.getOption(ToolPostOptions.TOOLPOST_OPTION_LINK)!!.asString
            val climaxTime = event.getOption(ToolPostOptions.TOOLPOST_OPTION_TRIM)?.asString

            log.info("Analyzing $link and parsing from $climaxTime")

            if (!link.contains("https://")) {
                event.reply("This is not a valid link nanora!").setEphemeral(true).queue()
                return
            }

            event.deferReply().queue()

            val fileDuration = getFileDuration(link)

            if (fileDuration < 16) {
                log.info("Too short: $link")
                event.hook.editMessage(content = "This song is too short nanora!").queue()
                return
            } else if (fileDuration >= 900) {
                log.info("Too long: $link")
                event.hook.editMessage(content = "This song is too long nanora! max length is 15 minutes nora.").queue()
                return
            }

            var timeToTrim = parseDurationToSeconds(climaxTime)

            if (timeToTrim != null && timeToTrim >= TOOLPOST_DROP) {
                if (timeToTrim >= fileDuration) {
                    // Null the option if time requested is higher than the actual song
                    timeToTrim = null
                } else {
                    // Minus 8.633 to sync the climax with the video's
                    timeToTrim -= TOOLPOST_DROP
                }
            }

            log.info("Creating toolpost from $link, climax: $climaxTime")

            val customToolPost = createToolPost(link, timeToTrim = timeToTrim ?: Random.nextDouble(0.0, fileDuration))

            if (customToolPost != null) {
                val video = FileUpload.fromData(customToolPost)
                event.hook.editMessage(content = "Created toolpost from <$link>, sending soon nora.").queue()
                event.messageChannel.send(files = listOf(video)).queue()
                customToolPost.delete()
            } else {
                log.error("Failed to fetch song from $link")
                event.hook.editMessage(content = "Failed to fetch song from link, maybe I'm not old enough to see this video nanora~")
                    .queue()
            }
        } else {
            val ownerName: String = event.jda.retrieveApplicationInfo().complete().owner.name
            log.error("original.mp4 is missing from data/toolpost/")
            event.hook.editMessage(content = "An important file is missing from the hosts' computer, annoy the HECK out of $ownerName nanora!")
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
            log.error("Audio file returned null from $link")
            return null
        }
    }

    private fun toolPostBaseVideoExists(): Boolean = File(PATH, ORIGINAL_VIDEO).exists()

    private fun makeMergeCommand(contentFileName: String, outputName: String): List<String> =
        FFMPEG_BASE_COMMAND + listOf(
            contentFileName,
            "-i",
            ORIGINAL_VIDEO,
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-map",
            "1:v:0", //Maps the first video stream from the second input
            "-map",
            "0:a:0", //Maps the first audio stream from the first input
            "-shortest",
            outputName
        )

    private fun makeTrimSecondsCommand(contentName: String, seconds: String, newContentName: String): List<String> =
        FFMPEG_BASE_COMMAND + listOf(contentName, "-ss", seconds) + listOf(
            "-vcodec",
            "copy",
            "-acodec",
            "copy",
            newContentName
        )

    private fun makeYtDlpDownloadCommand(link: String): List<String> =
        listOf(
            "yt-dlp",
            "--print",
            "filename",
            "--no-simulate",
            "--no-playlist",
            "-o",
            "%(id)s.%(ext)s",
            link
        )

    private fun makeYtDlpGetDurationCommand(link: String): List<String> =
        listOf(
            "yt-dlp",
            "--print",
            "duration",
            "--no-playlist",
            link
        )

    /**
     * Merges a file's audio with original.mp4's video and outputs it
     * @param fileName
     * @return the path to the merged video
     */
    private fun mergeContentWithVideo(fileName: String): File? {
        try {
            val now = Instant.now().toString().replaceInstantChars() + ".mp4"
            val command = makeMergeCommand(fileName, now)
            val child = spawnProcess(command)

            child.waitFor()

            return File("$PATH/$now")
        } catch (e: Exception) {
            log.error(e.toString())
            return null
        }
    }

    /**
     * Downloads a file using yt-dlp and outputs it in a folder
     * @return File name with the extension
     * @param link link to download the file from
     */
    private fun downloadContent(link: String): String? {
        try {
            val command = makeYtDlpDownloadCommand(link)
            val child = spawnProcess(command)

            child.waitFor()

            val stdout = child.inputStream.bufferedReader()
                .readText()
                .replace("\r", "")
                .replace("\n", ".")
                .replaceLast(".", "")

            return stdout.ifEmpty { null }
        } catch (e: Exception) {
            log.error(e.toString())
            return null
        }
    }

    /**
     * Trims seconds from the start of a file using ffmpeg and outputs it in a folder, replacing the previous content file
     * @param fileName
     * @param seconds Seconds to trim from the start
     */
    private fun trimContent(fileName: String, seconds: Double): String {
        var trimmedContentName = ""
        try {
            val newFileName = "trimmed-$fileName".replace(".mp4", ".mp3")
            val command = makeTrimSecondsCommand(fileName, seconds.toString(), newFileName)
            val child = spawnProcess(command)

            child.waitFor()

            File("$PATH/$fileName").delete()

            trimmedContentName = newFileName
        } catch (e: Exception) {
            log.error(e.toString())
        }
        return trimmedContentName
    }

    /**
     * Returns the length of the video in seconds
     * @param link
     * @return the... guess what? length of the video in seconds but it's Double
     */
    private fun getFileDuration(link: String): Double {
        try {
            val command = makeYtDlpGetDurationCommand(link)
            val child = spawnProcess(command)

            child.waitFor()

            val output = child.inputStream.bufferedReader().readText().clearCRLF()

            return if (output.isNotEmpty())
                output.toDouble()
            else
                0.0
        } catch (e: Exception) {
            log.error(e.toString())
        }
        return 0.0
    }

    private fun spawnProcess(command: List<String>): Process =
        ProcessBuilder(command)
            .directory(toolPostWorkingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
}