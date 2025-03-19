package org.matkija.bot.discordBot.abstracts

import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.utils.TsihPoggers


abstract class SlashCommand {

    protected abstract fun execute(event: GenericCommandInteractionEvent)

    fun tryExecute(event: GenericCommandInteractionEvent) = try {
        execute(event)
    } catch (e: Exception) {
        TsihPoggers.POG.error(e.toString())
        if (event.isAcknowledged) {
            event.hook.editMessage(content = "```\n${e.message}\n```").queue()
        } else {
            event.reply("```\n${e.message}\n```").setEphemeral(true).queue()
        }
    }
}