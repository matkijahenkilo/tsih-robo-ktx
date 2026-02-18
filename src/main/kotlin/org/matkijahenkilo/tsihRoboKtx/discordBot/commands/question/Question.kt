package org.matkijahenkilo.tsihRoboKtx.discordBot.commands.question

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts.SlashCommand

private val answers = listOf(
    "Of course nora!",
    "That's a strong yes nanora!",
    "Indeed nora!",
    "Ooooh!! Yes nora!!!",
    "Hmmmm... I guess nanora?",
    "Yes, that's based nanora!",

    "I don't think so nora.",
    "Teheheh~ no nanora.",
    "I think it's better not to answer nora.",
    "No, that's cringe nanora!",
    "Hell no, nanora.",
    "Of course not nanora!"
)

class Question(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val question = event.getOption(QUESTION_OPTION_FIELD)!!.asString
        event.reply("> $question\n${answers.random()}").queue()
    }

    companion object SlashOptions {
        const val QUESTION = "question"
        const val QUESTION_OPTION_FIELD = "words"

        fun getCommands(): SlashCommandData =
            Commands.slash(QUESTION, "I answer yes or no to a question~")
                .addOptions(
                    OptionData(OptionType.STRING, QUESTION_OPTION_FIELD, "Input your question here nanora!", true)
                )
    }
}