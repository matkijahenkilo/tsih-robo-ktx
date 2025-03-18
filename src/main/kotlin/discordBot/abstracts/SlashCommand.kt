package org.matkija.bot.discordBot.abstracts

import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent


abstract class SlashCommand {

    protected abstract fun execute(event: GenericCommandInteractionEvent)

    fun tryExecute(event: GenericCommandInteractionEvent) = try {
        execute(event)
    } catch (e: Exception) {
        e.printStackTrace()
        if (event.isAcknowledged) {
            event.hook.editMessage(content = "```\n${e.message}\n```").queue()
        } else {
            event.reply("```\n${e.message}\n```").setEphemeral(true).queue()
        }
    }
}