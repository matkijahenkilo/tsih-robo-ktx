package org.matkija.bot.discordBot.commands.question

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun questionInit(jda: JDA): SlashCommandData {
    jda.onCommand(QuestionSlashOptions.QUESTION) { event ->
        Question(event).tryExecute()
    }

    return QuestionSlashOptions.getCommands()
}