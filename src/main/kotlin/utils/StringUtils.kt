package org.matkija.bot.utils

fun String.clearCRLF(): String = this.replace("\n", "").replace("\r", "")