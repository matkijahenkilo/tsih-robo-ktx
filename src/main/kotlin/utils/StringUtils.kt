package org.matkija.bot.utils

fun String.clearCRLF(): String = this.replace("\n", "").replace("\r", "")

fun String.replaceInstantChars(): String = this
    .replace(":", "")
    .replace("-", "")
    .replace("T", "-")

fun String.replaceLast(toReplace: String, newChar: String): String =
    if (last() in toReplace)
        dropLast(1) + newChar
    else
        this