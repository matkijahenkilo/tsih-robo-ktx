package org.matkija.bot.discordBot.passiveCommands.sauceSender

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.io.File


class SauceSender(
    private val event: MessageReceivedEvent,
    private val content: String,
) {
    private val FOOTER_IMAGE = "tsih-icon.png"
    private val FOOTER_IMAGE_PATH = mutableListOf(FileUpload.fromData(File("data/tsih-icon.png")))

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
        val withFooterImage = uploads + FOOTER_IMAGE_PATH
        event.channel.send(
            files = if (payload.embedInfo != null) withFooterImage else uploads,
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

    //related to embeds
    //TODO: embed is not as wide as Discordia's, while being wider than it should be for Misskey links lol
    private fun buildEmbed(
        stdout: String,
        sourceLink: String,
        filesPath: MutableList<File>,
        desc: String? = null
    ): List<MessageEmbed> {
        val lines = stdout.lines()

        val e = EmbedInfo(
            nick = lines[0],
            username = lines[1],
            avatar = lines[2],
            retweet = lines[3],
            favourite = lines[4],
            timestamp = lines[5],
            text = lines.drop(6).joinToString("\n").trimEnd('\n')
        )

        // replace file path to be only the file name, otherwise discord doesn't embed the image
        val filesName = filesPath.map { it.toString().substringAfter("./data/temp/") }

        val embeds = mutableListOf(
            EmbedBuilder {
                color = 0x80fdff
//            timestamp = e.timestamp
                url = sourceLink
                author {
                    url = sourceLink
                    iconUrl = e.avatar
                    name = String.format("%s (@%s)", e.nick, e.username)
                }

                title = " "
                description = e.text

                field {
                    name = "Shares 🔁"
                    value = e.retweet
                }
                field {
                    name = "Favourites 💖"
                    value = e.favourite
                }

                footer {
                    iconUrl = "attachment://$FOOTER_IMAGE"
                    name = desc ?: "Tsih-Robo-KTX!"
                }
            }
        )

        var updatedEmbeds = emptyList<InlineEmbed>()
        if (!hasVideos(filesName))
            updatedEmbeds = addAdditionalAttachments(embeds, filesName)

        return buildEmbeds(updatedEmbeds.ifEmpty { embeds })
    }

    private fun addAdditionalAttachments(embeds: MutableList<InlineEmbed>, files: List<String>): List<InlineEmbed> {
        files.forEach { file ->
            embeds.add(EmbedBuilder {
                url = embeds[0].url
                title = embeds[0].title
                image =
                    "attachment://${file}" //if this doesn't work, be sure file doesn't have any schizo character that discord hates
            })
        }
        return embeds
    }

    private fun buildEmbeds(embeds: List<InlineEmbed>): List<MessageEmbed> = embeds.map { it.build() }

    private fun hasVideos(files: List<String>): Boolean =
        files.any { path ->
            setOf(".mp4", ".webm", ".mov").any { extension ->
                path.endsWith(
                    extension,
                    ignoreCase = true
                )
            }
        }

    private data class EmbedInfo(
        val nick: String,
        val username: String,
        val avatar: String,
        val retweet: String,
        val favourite: String,
        val timestamp: String,
        val text: String? = null,
    )
}