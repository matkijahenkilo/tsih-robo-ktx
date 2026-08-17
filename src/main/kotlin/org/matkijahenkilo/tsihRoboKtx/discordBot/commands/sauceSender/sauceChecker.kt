package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.sauceSender

import org.matkijahenkilo.tsihRoboKtx.utils.BotSettings

fun shouldIFixIt(content: String): Boolean = whiteList.any { content.contains(it, ignoreCase = true) }

fun List<String>.filterOutBlacklistedItems(): List<String> = this.filter { word -> blackList.none { it.matches(word) } }

val blackList = listOf(
    Regex("\\[.+]\\(.+\\)"),
    Regex("<.+>"),
)

private val whiteList = BotSettings.get<List<String>>(BotSettings.Settings.GALLERY_DL_WHITE_LIST)