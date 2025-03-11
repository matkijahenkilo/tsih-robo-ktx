package org.matkija.bot.utils

private const val red = "\u001b[31m"
private const val green = "\u001b[32m"
private const val blue = "\u001b[34m"
private const val cyan = "\u001b[96m"
private const val magenta = "\u001b[35m"
private const val yellow = "\u001b[33m"
private const val reset = "\u001b[0m"

fun red(str: String): String = String.format("%s%s%s", red, str, reset)

fun green(str: String): String = String.format("%s%s%s", green, str, reset)

fun blue(str: String): String = String.format("%s%s%s", blue, str, reset)

fun cyan(str: String): String = String.format("%s%s%s", cyan, str, reset)

fun magenta(str: String): String = String.format("%s%s%s", magenta, str, reset)

fun yellow(str: String): String = String.format("%s%s%s", yellow, str, reset)