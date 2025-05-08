package org.matkija.bot.discordBot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import org.matkija.bot.discordBot.commands.music.audio.GuildMusicManager
import org.matkija.bot.discordBot.commands.music.audio.TrackScheduler
import org.matkija.bot.utils.getRandomColor
import java.time.Instant

object MusicInfoEmbed {

    private var pausedText = "Paused:"
    private var shuffleText = "Shuffling:"
    private var repeatText = "Repeating:"

    private val mocks = listOf(
        "Listen to this banger!",
        "This one is is kinda meh but I'll play it anyway~",
        "this one is a masterpiece!",
        "T-Tesouro l-likes this one... 🤮",
        "You have a pitiful taste in music nora.",
        "Really?",
        "Pretty normie ngl nora."
    )

    private val userMocks = listOf(
        "%s asked for this shit",
        "%s asked for this crap",
        "%s asked for this noise",
        "%s has a terrible taste in music",
        "%s has a good taste in music",
        "what is this shit, %s?",
        "asked by %s",
        "%s, I expected more from you",
        "%s, really???",
        "%s, nanako has a better taste than you",
    )

    fun postEmbed(
        content: RequestedTrackInfo,
        channel: MessageChannel,
        trackScheduler: TrackScheduler,
        player: AudioPlayer
    ) = channel.send(
        components = loadComponents(),
        embeds = listOf(
            loadPlayingEmbed(
                content.requester!!,
                content.track!!.info,
                trackScheduler,
                player.isPaused
            )
        )
    ).queue()

    private fun loadComponents(): List<LayoutComponent> {
        val stop = danger(MusicSlashCommands.STOP, emoji = Emoji.fromUnicode("🛑"))
        val pauseOrStart = primary(MusicSlashCommands.PLAY, emoji = Emoji.fromUnicode("⏯"))
        val skip = primary(MusicSlashCommands.SKIP, emoji = Emoji.fromUnicode("⏭"))
        val repeat = primary(MusicSlashCommands.REPEAT, emoji = Emoji.fromUnicode("🔂"))
        val shuffle = primary(MusicSlashCommands.SHUFFLE, emoji = Emoji.fromUnicode("🔀"))
        return row(stop, pauseOrStart, skip, repeat, shuffle).into()
    }

    private fun loadPlayingEmbed(
        user: User,
        info: AudioTrackInfo,
        scheduler: TrackScheduler,
        isPaused: Boolean
    ): MessageEmbed {
        var embed = EmbedBuilder {
            thumbnail = info.artworkUrl
            title = info.title + "\n(${getTimestamp(info.length)})"
            url = info.uri
            color = getRandomColor()
            timestamp = Instant.now()
            description = mocks.random()
            footer {
                name = String.format(userMocks.random(), user.name)
                iconUrl = user.avatarUrl
            }
            author {
                name = info.author
                iconUrl = null
            }
        }
        embed = fillFields(embed, scheduler, isPaused)
        return embed.build()
    }

    fun getUpdatedEmbed(event: ButtonInteractionEvent, guildMusicManager: GuildMusicManager): MessageEmbed {
        val e = event.message.embeds[0]
        val player = guildMusicManager.player
        val scheduler = guildMusicManager.scheduler
        var embed = EmbedBuilder {
            title = e.title
            url = e.url
            color = e.colorRaw
            timestamp = e.timestamp
            description = e.description
            footer {
                name = e.footer!!.text!!
                iconUrl = e.footer!!.iconUrl
            }
            author {
                name = e.author!!.name
                iconUrl = e.author!!.iconUrl
            }
        }
        if (e.thumbnail != null) embed.thumbnail = e.thumbnail!!.url
        embed = fillFields(embed, scheduler, player.isPaused)
        return embed.build()
    }

    fun fillFields(embed: InlineEmbed, scheduler: TrackScheduler, isPaused: Boolean): InlineEmbed {
        embed.field {
            name = pausedText
            value = if (isPaused) "✅" else "❌"
            inline = true
        }
        embed.field {
            name = repeatText
            value = if (scheduler.isRepeating) "✅" else "❌"
            inline = true
        }
        embed.field {
            name = shuffleText
            value = if (scheduler.isShuffled) "✅" else "❌"
            inline = true
        }
        return embed
    }
}