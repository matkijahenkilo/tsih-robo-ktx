package org.matkija.bot.discordBot.commands.avatar

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand

class Avatar : SlashCommand() {
    override fun execute(event: GenericCommandInteractionEvent) {
        val user = event.getOption(AvatarSlashOptions.AVATAR_OPTION_USER)
        val avatarUrl =
            (if (user?.asUser != null) user.asUser.effectiveAvatarUrl else event.user.effectiveAvatarUrl) + "?size=2048"
        event.reply_(
            embeds = listOf(
                EmbedBuilder {
                    title = "This is a nice avatar nanora!"
                    color = 0xff80fd
                    field {
                        name = "What a lazy adult!"
                        value = "They look like holding a lot of shiny stars..."
                    }
                    image = avatarUrl
                }.build()
            )
        ).queue()
    }
}