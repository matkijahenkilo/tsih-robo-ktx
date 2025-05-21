package org.matkija.bot.discordBot.hybridCommands.markov

private val linkPatters = Regex("<\\bhttps://[^\\s>]+(?=>)>|\\bhttps://\\S+|<\\bhttp://[^\\s>]+(?=>)>|\\bhttp://\\S+")
private val mentionPattern = Regex("<@[0-9]+>")
private val roleMentionPattern = Regex("<@&[0-9]+>")
private const val theMostAnnoyingPattern = "@everyone"
private const val theLesserAnnoyingPattern = "@ everyone"
private val spacePattern = Regex("\\s+")

private fun String.filterOutLinks(): String = this.replace(linkPatters, "")
private fun String.filterOutMentionPatterns(): String =
    this.replace(mentionPattern, "").replace(roleMentionPattern, "").replace(theMostAnnoyingPattern, theLesserAnnoyingPattern)

private fun String.replaceDoubleSpacesIntoSingularSpace(): String = this.replace(spacePattern, " ")

fun String.clearForMarkovCorpus(): String = this
    .replace("\n", " ")
    .filterOutLinks()
    .filterOutMentionPatterns()
    .replaceDoubleSpacesIntoSingularSpace()
    .trim()