package org.matkija.bot.discordBot.commands.messageDelete

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object MessageDeleteCommand {
    const val MESSAGE_COMMAND_DELETE_MESSAGE = "delete tsih's message"

    fun getCommands(): CommandData = Commands.message(MESSAGE_COMMAND_DELETE_MESSAGE)
}