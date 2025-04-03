package discordBot.commands.toolPost

import org.matkija.bot.utils.clearCRLF
import java.io.File
import java.time.Instant


const val AUDIO_FORMAT = ".mp3"
const val ORIGINAL_VIDEO = "original.mp4"

//val FFMPEG_MERGE_COMMAND = listOf(
//    "ffmpeg",
//    "-y",
//    "-i %s",
//    "-i $ORIGINAL_VIDEO",
//    "-c:v copy",
//    "-c:a aac",
//    "-shortest",
//    OUTPUT_NAME
//)
const val FFMPEG_MERGE_COMMAND = "ffmpeg -y -i %s -i $ORIGINAL_VIDEO -c:v copy -c:a aac -shortest %s"
const val FFMPEG_TRIM_SECONDS_COMMAND = "ffmpeg -y -i %s -ss %s -vcodec copy -acodec copy %s"
const val YT_DLP_COMMAND = "yt-dlp -x --audio-format mp3 --print filename --no-simulate --no-playlist -o %(id)s REPLACE"

const val PATH = "data/toolpost"
val workingDir = File(PATH)

/**
 * Merges an audio file with video original.mp4 and outputs it in assets/
 * @param audioFileName The audio file name
 * @return the File object of the merged video
 */
fun mergeAudioWithVideo(audioFileName: String): File? {
    try {
        val now = Instant.now().toString().replace(":", "").replace("-", "").replace("T", "-") + ".mp4"
        val command = FFMPEG_MERGE_COMMAND.format(audioFileName, now)
        val parts = command.split("\\s".toRegex())
        val child = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        child.waitFor()

        return File("$PATH/$now")
    } catch (e: Exception) {
        e.toString()
        return null
    }
}

/**
 * Downloads an audio file as .mp3 using yt-dlp and outputs it in a folder
 * @return File name with the extension
 */
fun downloadAudio(link: String): String? {
    try {
        val command = YT_DLP_COMMAND.replace("REPLACE", link) // lol
        val parts = command.split("\\s".toRegex())
        val child = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        child.waitFor()

        val stdout = child.inputStream.bufferedReader().readText().clearCRLF()

        return if (stdout.isEmpty()) {
            null
        } else {
            stdout + AUDIO_FORMAT
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

//todo: get audio length to stop command if length < 10 seconds
/**
 * Trims seconds from the start of an audio using ffmpeg and outputs it in a folder, replacing the previous audio file
 * @param audio File name
 * @param seconds Seconds to trim from the start
 */
fun trimAudio(audio: String, seconds: Double): String {
    var trimmedAudioName = ""
    try {
        val newAudioName = "trimmed-$audio".replace(".mp4", ".mp3")
        val command = FFMPEG_TRIM_SECONDS_COMMAND.format(audio, seconds, newAudioName)
        val parts = command.split("\\s".toRegex()) // lol
        val child = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        child.waitFor()

        File("$PATH/$audio").delete()

        trimmedAudioName = newAudioName
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return trimmedAudioName
}

fun baseVideoExists(): Boolean = File(PATH, ORIGINAL_VIDEO).exists()