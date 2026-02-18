package org.matkijahenkilo.tsihRoboKtx.discordBot.abstracts

import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkijahenkilo.tsihRoboKtx.LOG

abstract class SlashCommand(private val event: GenericCommandInteractionEvent) {

    protected abstract fun execute()

    fun tryExecute() = try {
        execute()
    } catch (e: Exception) {
        LOG.error(e.toString())
        if (event.isAcknowledged) {
            event.hook.editMessage(content = "```\n${e.message}\n```").queue()
        } else {
            event.reply("```\n${e.message}\n```").setEphemeral(true).queue()
        }
    }
}