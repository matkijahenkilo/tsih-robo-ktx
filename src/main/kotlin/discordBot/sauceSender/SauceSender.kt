package org.matkija.bot.discordBot.sauceSender

import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.io.File

const val FOOTER_IMAGE = "tsih-icon.png"
val FOOTER_IMAGE_PATH = mutableListOf(FileUpload.fromData(File("data/tsih-icon.png")))

// prefer to download
// TODO: if >10mb just link it
val list = listOf(
    "https://x.com/",
    "https://hitomi.la/",
    "https://kemono.su/",
    "https://twitter.com/",
    "https://sankaku.app/",
    "https://exhentai.org/",
    "https://e-hentai.org/",
    "https://kemono.party/",
    "https://inkbunny.net/",
    "https://www.pixiv.net/",
    "https://www.tsumino.com/",
    "https://www.deviantart.com/",
    "https://chan.sankakucomplex.com/",

    "https://e621.net/",
    "https://booru.io/",
    "https://pawoo.net/",
    "https://misskey.io/",
    "https://nijie.info/",
    "https://nhentai.net/",
    "https://e-hentai.org/",
    "https://hentai2read.com/"
)

fun canIFixIt(content: String): Boolean = list.any { content.contains(it, ignoreCase = true) }

class SauceSender(
    private val event: MessageReceivedEvent,
    private val content: String,
) {
    fun sendSauce() = runBlocking {
        val links = filterOutWords(content).distinct()

        val jobList = mutableListOf<Job>()
        links.forEach { link ->
            jobList += async {
                val payload = Payload()
                val command: List<String>
                var infoCommand: List<String> = emptyList()

                // make command for each website, and get info for embed if possible
                when {
                    isTwitterLink(link) -> {
                        command = makeTwitterCommand(link)
                        infoCommand = makeTwitterCommand(link, true)
                    }

                    isMisskeyLink(link) -> {
                        command = makeMisskeyCommand(link)
                        infoCommand = makeMisskeyCommand(link, true)
                    }

                    else -> {
                        command = makeCommonCommand(link)
                    }
                }

                // organize files to list
                val child = spawnProcess(command)
                val filesStdout = readProcess(child).stdout
                val files = mutableListOf<File>()
                filesStdout.lines().forEach { filePath ->
                    if (filePath.isNotEmpty()) {
                        val clearFilePath = filePath
                            .replace("\n", "")
                            .replace("\r", "")
                            .replace("\\", "/")
                            .replace("# ", "")
                            .replace("./", "/")
                        val file = File("./data", clearFilePath)
                        if (file.exists() && file.length() < 10 * 1024 * 1024)
                            files.add(file)
                    }
                }

                // if failing to fetch anything
                if (files.isEmpty()) {
                    child.destroy()
                    return@async
                }
                payload.files = files

                // deal with embed
                if (infoCommand.isNotEmpty()) {
                    val infoChild = spawnProcess(infoCommand)
                    val out = readProcess(infoChild).stdout
                    payload.embedInfo = buildEmbed(out, link, payload.files!!)
                }

                // send it, embed or not
                // TODO: refer the previous message
                if (payload.files!!.size > 10) {
                    sendFilesInParts(payload)
                } else {
                    sendFiles(payload)
                }

                child.destroy() // readProcess has child.exit(), this ensures le fokin process is dead
            }
        }
        jobList.joinAll()
    }

    // no embed for the wicked
    private fun sendFilesInParts(payload: Payload) {
        val uploads = mutableListOf<FileUpload>()
        payload.files!!.forEach { file ->
            uploads.add(FileUpload.fromData(file))
            if (uploads.size == 10) {
                event.channel.send(files = uploads).queue()
                uploads.clear()
            }
        }
    }

    private fun sendFiles(payload: Payload) {
        val uploads = mutableListOf<FileUpload>()
        payload.files!!.forEach { file ->
            uploads.add(FileUpload.fromData(file))
        }
        event.channel.send(
            files = uploads + FOOTER_IMAGE_PATH,
            embeds = if (payload.embedInfo != null) payload.embedInfo!! else emptyList()
        ).queue()
    }

    private fun filterOutWords(content: String): List<String> =
        content.split(" ").filter { it.contains("https://") }.toList()

    // TODO: each server and room should have their own custom limit
    private fun makeTwitterCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
        baseArgs + if (shouldGetInfoOnly) infoArgs(link, twitterFilters) else downloadArgs(link, limit)

    private fun makeMisskeyCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
        baseArgs + if (shouldGetInfoOnly) infoArgs(link, misskeyFilters) else downloadArgs(link, limit)

    private fun downloadArgs(link: String, limit: Int): List<String> =
        listOf("--range", "1-$limit", "--ugoira-conv", "-D", "./temp/", link)

    private fun infoArgs(link: String, filter: String): List<String> =
        listOf("--filter", filter, "-s", link)

    private fun makeCommonCommand(link: String) = baseArgs + downloadArgs(link, 5)

    private fun isTwitterLink(s: String) = "https://twitter.com" in s || "https://x.com" in s
    private fun isMisskeyLink(s: String): Boolean = "https://misskey.io" in s

    private val baseArgs = listOf("gallery-dl", "--cookies", "./cookies.txt")
    private val twitterFilters =
        "print(user['name']) or print(user['nick']) or print(user['profile_image']) or print(retweet_count) or print(favorite_count) or print(date) or print(content) or abort()"
    private val misskeyFilters =
        "print(user['name']) or print(user['username']) or print(user['avatarUrl']) or print(renoteCount) or print(reactionCount) or print(date) or print(text) or abort()"


    // processes
    private data class ProcessOutput(
        val stdout: String,
        val stderr: String
    )

    private fun spawnProcess(command: List<String>): Process = ProcessBuilder(command)
        .directory(File("./data"))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    private suspend fun readProcess(child: Process): ProcessOutput = coroutineScope {
        child.waitFor()
        ProcessOutput(
            child.inputStream.bufferedReader().readText().replace("\r", ""),
            child.errorStream.bufferedReader().readText().replace("\r", "")
        )
    }

    private data class Payload(
        var files: MutableList<File>? = null,
        var embedInfo: List<MessageEmbed>? = null,
    )
}