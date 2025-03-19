package org.matkija.bot.discordBot.commands.avatar

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand

class Avatar : SlashCommand() {
    override fun execute(event: GenericCommandInteractionEvent) {
        val user = event.getOption(AvatarSlashOptions.AVATAR_OPTION_USER)
        event.reply_(
            embeds = listOf(
                EmbedBuilder {
                    description = "Fascinating..."
                    color = 0xff80fd
                    image = if (user?.asUser != null) user.asUser.avatarUrl else event.user.avatarUrl
                }.build()
            )
        ).queue()
    }
}