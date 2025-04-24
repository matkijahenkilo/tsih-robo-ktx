package org.matkija.bot.discordBot.passiveCommands.sauceSender

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.matkija.bot.utils.clearCRLF
import org.matkija.bot.utils.getRandomColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

// TODO: each server and room should have their own custom limit
// TODO: if >10mb just link it
class SauceSender(
    private val event: MessageReceivedEvent,
    content: String,
) {
    private val footerImage = "sauce-footer.png"
    private var footerImagePath: MutableList<FileUpload>? = null
    private val discordSizeLimit = 10
    private val links: List<String> = filterOutWords(content).distinct()
    private val logger: Logger = LoggerFactory.getLogger(SauceSender::class.java)

    init {
        val footerImage = File("data/images/$footerImage")
        if (footerImage.exists())
            footerImagePath = mutableListOf(FileUpload.fromData(footerImage))
    }

    fun sendSauce() = runBlocking {

        val jobList = mutableListOf<Job>()
        var altTwitterFix = AltTwitterFix("", true)

        links.forEach { link ->
            if (isTwitterLink(link)) {
                altTwitterFix = isTwitterWorking(link)
                return@forEach
            }
        }

        links.forEach { link ->
            jobList += async {
                if (isTwitterLink(link) && !altTwitterFix.worksOrHasMedia) {
                    logger.info("Sending alternative fix for $link")
                    sendAlternativeFix(link, altTwitterFix.errorMsg)
                } else {
                    val payload = Payload()
                    val command: List<String>
                    var infoCommand: List<String> = emptyList()
                    var website: String? = null

                    // make command for each website, and get info for embed if possible
                    when {
                        isTwitterLink(link) -> {
                            command = makeTwitterCommand(link)
                            infoCommand = makeTwitterCommand(link, true)
                            website = "Twitter.com"
                        }

                        isMisskeyLink(link) -> {
                            command = makeMisskeyCommand(link)
                            infoCommand = makeMisskeyCommand(link, true)
                            website = "Misskey.io"
                        }

                        else -> {
                            command = makeCommonCommand(link)
                        }
                    }

                    // download and organize files to list
                    val child = spawnProcess(command)
                    val exitedChild = readProcess(child)
                    val filesStdout = exitedChild.stdout
                    val files = mutableListOf<File>()
                    filesStdout.lines().forEach { filePath ->
                        if (filePath.isNotEmpty()) {
                            val clearFilePath = filePath
                                .clearCRLF()
                                .replace("\\", "/")
                                .replace("# ", "")
                                .replace("./", "/")
                            val file = File("./data", clearFilePath)
                            if (file.exists() && file.length() < discordSizeLimit * 1024 * 1024)
                                files.add(file)
                            else
                                logger.error("File $file was bigger than ${discordSizeLimit}mb")
                        }
                    }

                    // if failing to fetch anything
                    if (files.isEmpty()) {
                        child.destroy()
                        logger.error("Failed to fetch anything from $link: ${exitedChild.stderr.clearCRLF()}")
                        return@async
                    }
                    payload.files = files

                    // deal with embed
                    if (infoCommand.isNotEmpty()) {
                        val infoChild = spawnProcess(infoCommand)
                        val infoStdout = readProcess(infoChild).stdout
                        payload.embedInfo = buildEmbed(infoStdout, link, payload.files!!, website)
                    }

                    logger.info("Sending content from $link")

                    // send it, embed or not
                    if (payload.files!!.size > 10) {
                        sendFilesInParts(payload, link)
                    } else {
                        sendFiles(payload, link)
                    }

                    child.destroy() // readProcess has child.exit(), this ensures le fokin process is dead
                }
            }
        }
        jobList.joinAll()

        //delete user's message embed
        if (!event.message.isSuppressedEmbeds)
            event.message.suppressEmbeds(true).queue()
    }

    // as far as I know, misskey is the only website that allows users to send more
    // than 4 images per post while being a mastodon fork, because this is uncommon
    // I'd rather treat misskey links with more than 4 images as common websites that
    // doesn't need embeds
    private fun sendFilesInParts(payload: Payload, link: String) {
        val uploads = mutableListOf<FileUpload>()
        payload.files!!.forEach { file ->
            uploads.add(FileUpload.fromData(file))
            if (uploads.size == 10) {
                event.message.reply_(
                    content = if (links.size > 1) "<$link>" else "",
                    files = uploads
                ).mentionRepliedUser(false).queue()
                uploads.clear()
            }
        }
    }

    private fun sendFiles(payload: Payload, link: String) {
        val uploads = mutableListOf<FileUpload>()
        payload.files!!.forEach { file ->
            uploads.add(FileUpload.fromData(file))
        }

        var withFooterImage = uploads
        if (footerImagePath != null) {
            withFooterImage = (uploads + footerImagePath!!).toMutableList()
        }

        event.message.reply_(
            content = if (links.size > 1) "<$link>" else "",
            files = if (payload.embedInfo != null) withFooterImage else uploads,
            embeds = if (payload.embedInfo != null) payload.embedInfo!! else emptyList()
        ).mentionRepliedUser(false).queue()
    }


    /*
    related to checking if Twitter is working and sending alternative fix
     */

    // empty embeds doesn't always mean that it is sensitive, for some twitter™️ reason (￣ー￣)
    private suspend fun isTwitterWorking(link: String): AltTwitterFix {
        val child = spawnProcess(makeMediaCheckCommand(link))
        val readChild = readProcess(child)
        // if -g returns a "No results for" then twitter works but no media was in the post
        return if (readChild.stderr.clearCRLF().contains("No results for", true)) {
            AltTwitterFix("", false)
        } else if (readChild.stderr.clearCRLF().contains("AuthenticationError", true)) {
            logger.error("Twitter is fucking with me: ${readChild.stderr}")
            val ownerName: String = event.jda.retrieveApplicationInfo().complete().owner.name
            AltTwitterFix(
                "-# ||I got an authentication error, please annoy the FUCK out of $ownerName to fix me!!||",
                false
            )
        } else {
            AltTwitterFix("", true)
        }
    }

    private fun sendAlternativeFix(link: String, content: String) {
        val newLink = link.replace(
            if (link.contains("twitter.com")) {
                "twitter.com"
            } else {
                "x.com"
            }, "stupidpenisx.com"
        )
        event.message.reply_(content = "$content\n$newLink")
            .mentionRepliedUser(false)
            .queue()
    }

    data class AltTwitterFix(
        val errorMsg: String,
        val worksOrHasMedia: Boolean
    )


    /*
    related to making commands for gallery-dl and preparing list of link inputs
     */

    private fun filterOutWords(content: String): List<String> =
        content.split(" ").filter { it.contains("https://") }.toList()

    private fun makeTwitterCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
        baseArgs + if (shouldGetInfoOnly) infoArgs(link, twitterFilters) else downloadArgs(link, limit)

    private fun makeMisskeyCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
        baseArgs + if (shouldGetInfoOnly) infoArgs(link, misskeyFilters) else downloadArgs(link, limit)

    private fun downloadArgs(link: String, limit: Int): List<String> =
        listOf("--range", "1-$limit", "--ugoira-conv", "-D", "./temp/", link)

    private fun infoArgs(link: String, filter: String): List<String> =
        listOf("--filter", filter, "-s", link)

    private fun makeCommonCommand(link: String) = baseArgs + downloadArgs(link, 5)

    private fun makeMediaCheckCommand(link: String): List<String> =
        baseArgs + listOf("-g", "-s", link)

    private fun isTwitterLink(s: String): Boolean = "https://twitter.com" in s || "https://x.com" in s
    private fun isMisskeyLink(s: String): Boolean = "https://misskey.io" in s

    private val baseArgs = listOf("gallery-dl", "--cookies", "./cookies.txt")
    private val twitterFilters =
        "print(user['name']) or print(user['nick']) or print(user['profile_image']) or print(retweet_count) or print(favorite_count) or print(date) or print(content) or abort()"
    private val misskeyFilters =
        "print(user['name']) or print(user['username']) or print(user['avatarUrl']) or print(renoteCount) or print(reactionCount) or print(date) or print(text) or abort()"


    /*
    related to processes
     */

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


    /*
    related to embeds
     */
    private fun buildEmbed(
        stdout: String,
        sourceLink: String,
        filesPath: MutableList<File>,
        footerText: String? = null
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
        val filesNames: MutableList<String> =
            filesPath.map { it.toString().substringAfter("./data/temp/") }.toMutableList()

        val embeds = mutableListOf(
            EmbedBuilder {
                color = getRandomColor()
//            timestamp = e.timestamp
                url = sourceLink
                author {
                    url = sourceLink
                    iconUrl = e.avatar
                    name = String.format("%s (@%s)", e.username, e.nick)
                }

                title = "   "
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
                    iconUrl = "attachment://$footerImage"
                    name = footerText ?: "tsih-robo-ktx~"
                }
            }
        )

        var updatedEmbeds = emptyList<InlineEmbed>()
        if (!hasVideos(filesNames)) // don't embed videos, otherwise it won't play
            updatedEmbeds = addAdditionalAttachments(embeds, filesNames)

        return buildEmbeds(updatedEmbeds.ifEmpty { embeds })
    }

    private fun addAdditionalAttachments(
        embeds: MutableList<InlineEmbed>,
        files: MutableList<String>
    ): List<InlineEmbed> {
        embeds[0].image = "attachment://${files[0]}" //lol
        files.removeAt(0)
        files.forEach { file ->
            embeds.add(EmbedBuilder {
                url = embeds[0].url
                title = embeds[0].title
                //if this doesn't work, be sure file doesn't have any schizo character that discord hates
                image = "attachment://${file}"
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