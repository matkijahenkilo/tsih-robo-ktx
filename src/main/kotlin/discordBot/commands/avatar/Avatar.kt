package org.matkija.bot.discordBot.commands.avatar

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.matkija.bot.discordBot.abstracts.SlashCommand
import org.matkija.bot.utils.getRandomColor

class Avatar(private val event: GenericCommandInteractionEvent) : SlashCommand(event) {
    override fun execute() {
        val member = event.getOption(AvatarSlashOptions.AVATAR_OPTION_MEMBER)?.asMember
        val asUser = event.getOption(AvatarSlashOptions.AVATAR_OPTION_AS_USER)?.asBoolean ?: false
        val avatarUrl =
            (if (asUser) member?.user?.effectiveAvatarUrl else member?.effectiveAvatarUrl
                ?: event.user.effectiveAvatarUrl) + "?size=2048"
        event.reply_(
            embeds = listOf(
                EmbedBuilder {
                    title = "This is a nice avatar nanora!"
                    color = getRandomColor()
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