package org.matkija.bot.discordBot.commands.question

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand

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
        val question = event.getOption(QuestionSlashOptions.QUESTION_OPTION_FIELD)!!.asString
        event.reply("> $question\n${answers.random()}").queue()
    }
}