package org.matkija.bot.discordBot.passiveCommands.sauceSender

fun shouldIFixIt(content: String): Boolean = whiteList.any { content.contains(it, ignoreCase = true) }

fun List<String>.filterOutBlacklistedItems(): List<String> = this.filter { word -> blackList.none { it.matches(word) } }

val blackList = listOf(
    Regex("\\[.+]\\(.+\\)"),
    Regex("<.+>"),
)

private val whiteList = listOf(
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
    "https://misskey.io/",
    "https://nijie.info/",
    "https://nhentai.net/",
    "https://e-hentai.org/",
    "https://hentai2read.com/"
)