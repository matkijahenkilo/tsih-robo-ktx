package org.matkija.bot.discordBot.commands.messageDelete

import dev.minn.jda.ktx.events.onContext
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun messageDeleteInit(jda: JDA): CommandData {

    val logger: Logger = LoggerFactory.getLogger("messageDeleteInit")

    jda.onContext<Message>(MessageDeleteCommand.MESSAGE_COMMAND_DELETE_MESSAGE) { event ->
        if (event.target.member?.idLong == event.jda.selfUser.idLong) {
            logger.info("${event.user.name} asked for message deletion in #${event.messageChannel.name}")
            event.reply("Ogei nanora, deleting my own message~").setEphemeral(true).queue()
            event.target.delete().queue()
        } else {
            event.reply("I won't delete messages of other users nanora!").setEphemeral(true).queue()
        }
    }

    return MessageDeleteCommand.getCommands()
}