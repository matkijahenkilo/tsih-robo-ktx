package org.matkija.bot.utils

private const val red = "\u001b[31m"
private const val green = "\u001b[32m"
private const val blue = "\u001b[34m"
private const val cyan = "\u001b[96m"
private const val magenta = "\u001b[35m"
private const val yellow = "\u001b[33m"
private const val reset = "\u001b[0m"

private val colors = listOf(
    0xA7F3E1,
    0xF00707,
    0xF07807,
    0xF5D50E,
    0x9FEF0B,
    0x2AEF0B,
    0x0BEF80,
    0x0BEFD3,
    0x0A97E8,
    0x0A2CE8,
    0x610AE8,
    0xC60AE8,
    0x970AE8,
    0xE80AD0,
    0xF20D8A,
    0xF20D4B,
    0xEB0808,
    0x030303,
    0xFFFFFF,
    0xff80fd,
    0xfffd80,
    0x80fdff,
    0x80fffd,
    0xfdff80,
    0xfd80ff
)

fun red(str: String): String = String.format("%s%s%s", red, str, reset)

fun green(str: String): String = String.format("%s%s%s", green, str, reset)

fun blue(str: String): String = String.format("%s%s%s", blue, str, reset)

fun cyan(str: String): String = String.format("%s%s%s", cyan, str, reset)

fun magenta(str: String): String = String.format("%s%s%s", magenta, str, reset)

fun yellow(str: String): String = String.format("%s%s%s", yellow, str, reset)

fun getRandomColor(): Int = colors.random()