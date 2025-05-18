package org.matkija.bot.discordBot.passiveCommands.sauceSender

import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.MessageEmbed
import java.io.File

fun spawnGallerydlProcess(command: List<String>): Process = ProcessBuilder(command)
    .directory(File("./data"))
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .redirectError(ProcessBuilder.Redirect.PIPE)
    .start()

suspend fun readGallerydlProcess(child: Process): ProcessOutput = coroutineScope {
    child.waitFor()
    ProcessOutput(
        child.inputStream.bufferedReader().readText().replace("\r", ""),
        child.errorStream.bufferedReader().readText().replace("\r", "")
    )
}

val baseArgs = listOf("gallery-dl", "--cookies", "./cookies.txt")

fun filterOutWords(content: String): List<String> =
    content.split(" ").filter { it.contains("https://") }.toList()

fun makeTwitterCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
    baseArgs + if (shouldGetInfoOnly) infoArgs(link, twitterFilters) else downloadArgs(link, limit)

fun makeMisskeyCommand(link: String, shouldGetInfoOnly: Boolean = false, limit: Int = 5): List<String> =
    baseArgs + if (shouldGetInfoOnly) infoArgs(link, misskeyFilters) else downloadArgs(link, limit)

fun downloadArgs(link: String, limit: Int): List<String> =
    listOf("--range", "1-$limit", "--ugoira-conv", "-D", "./temp/", link)

fun infoArgs(link: String, filter: String): List<String> =
    listOf("--filter", filter, "-s", link)

fun makeCommonCommand(link: String) = baseArgs + downloadArgs(link, 5)

fun makeMediaCheckCommand(link: String): List<String> =
    baseArgs + listOf("-g", "-s", link)

fun isTwitterLink(s: String): Boolean = "https://twitter.com" in s || "https://x.com" in s

fun isMisskeyLink(s: String): Boolean = "https://misskey.io" in s

fun isPixivLink(s: String): Boolean = "https://www.pixiv.net" in s

private const val twitterFilters =
    "print(user['name']) or print(user['nick']) or print(user['profile_image']) or print(retweet_count) or print(favorite_count) or print(date) or print(content) or abort()"

private const val misskeyFilters =
    "print(user['name']) or print(user['username']) or print(user['avatarUrl']) or print(renoteCount) or print(reactionCount) or print(date) or print(text) or abort()"

data class ProcessOutput(
    val stdout: String,
    val stderr: String
)

data class Payload(
    var files: MutableList<File>? = null,
    var embedInfo: List<MessageEmbed>? = null,
)