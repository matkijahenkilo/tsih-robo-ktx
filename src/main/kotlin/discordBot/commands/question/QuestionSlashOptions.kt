package org.matkija.bot.discordBot.commands.question

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object QuestionSlashOptions {
    const val QUESTION = "question"
    const val QUESTION_OPTION_FIELD = "words"

    fun getCommands(): SlashCommandData =
        Commands.slash(QUESTION, "I answer yes or no to a question~")
            .addOptions(
                OptionData(OptionType.STRING, QUESTION_OPTION_FIELD, "Input your question here nanora!", true)
            )
}