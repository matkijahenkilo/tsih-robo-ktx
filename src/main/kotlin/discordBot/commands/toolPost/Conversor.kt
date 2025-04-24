package discordBot.commands.toolPost

import java.io.File
import java.time.Instant


const val ORIGINAL_VIDEO = "original.mp4"

val FFMPEG_BASE_COMMAND = listOf("ffmpeg", "-y", "-i")

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

const val PATH = "data/toolpost"
val workingDir = File(PATH)

private fun String.replaceInstantChars(): String = this
    .replace(":", "")
    .replace("-", "")
    .replace("T", "-")

private fun String.replaceLast(toReplace: String, newChar: String): String =
    if (last() in toReplace)
        dropLast(1) + newChar
    else
        this

/**
 * Merges a file's audio with original.mp4's video and outputs it
 * @param fileName
 * @return the path to the merged video
 */
fun mergeContentWithVideo(fileName: String): File? {
    try {
        val now = Instant.now().toString().replaceInstantChars() + ".mp4"
        val command = makeMergeCommand(fileName, now)
        val child = spawnProcess(command)

        child.waitFor()

        return File("$PATH/$now")
    } catch (e: Exception) {
        ToolPost.POG.error(e.toString())
        return null
    }
}

/**
 * Downloads a file using yt-dlp and outputs it in a folder
 * @return File name with the extension
 * @param link link to download the file from
 */
fun downloadContent(link: String): String? {
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
        ToolPost.POG.error(e.toString())
        return null
    }
}

/**
 * Trims seconds from the start of a file using ffmpeg and outputs it in a folder, replacing the previous content file
 * @param fileName
 * @param seconds Seconds to trim from the start
 */
fun trimContent(fileName: String, seconds: Double): String {
    var trimmedContentName = ""
    try {
        val newFileName = "trimmed-$fileName".replace(".mp4", ".mp3")
        val command = makeTrimSecondsCommand(fileName, seconds.toString(), newFileName)
        val child = spawnProcess(command)

        child.waitFor()

        File("$PATH/$fileName").delete()

        trimmedContentName = newFileName
    } catch (e: Exception) {
        ToolPost.POG.error(e.toString())
    }
    return trimmedContentName
}

private fun spawnProcess(command: List<String>): Process =
    ProcessBuilder(command)
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

fun baseVideoExists(): Boolean = File(PATH, ORIGINAL_VIDEO).exists()