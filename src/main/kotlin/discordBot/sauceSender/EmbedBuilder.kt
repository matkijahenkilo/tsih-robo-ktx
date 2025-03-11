package org.matkija.bot.discordBot.sauceSender

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import net.dv8tion.jda.api.entities.MessageEmbed
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.TemporalAccessor

//TODO: embed is not as wide as Discordia's, while being wider than it should be for Misskey links lol
fun buildEmbed(
    stdout: String,
    sourceLink: String,
    filesPath: MutableList<File>,
    desc: String? = null
): List<MessageEmbed> {
    val lines = stdout.lines()

    val e = EmbedInfo(
        nick = lines[0],
        username = lines[1],
        avatar = lines[2],
        retweet = lines[3],
        favourite = lines[4],
        timestamp = lines[5],
        text = lines.drop(6).joinToString("\n").trimEnd('\n')
    )

    // replace file path to be only the file name, otherwise discord doesn't embed the image
    val filesName = filesPath.map { it.toString().substringAfter("./data/temp/") }

    val embeds = mutableListOf(
        EmbedBuilder {
            color = 0x80fdff
//            timestamp = e.timestamp
            url = sourceLink
            author {
                url = sourceLink
                iconUrl = e.avatar
                name = String.format("%s (@%s)", e.nick, e.username)
            }

            title = " "
            description = e.text

            field {
                name = "Shares 🔁"
                value = e.retweet
            }
            field {
                name = "Favourites 💖"
                value = e.favourite
            }

            footer {
                iconUrl = "attachment://${FOOTER_IMAGE}"
                name = desc ?: "Tsih-Robo-KTX!"
            }
        }
    )

    var updatedEmbeds = emptyList<InlineEmbed>()
    if (!hasVideos(filesName))
        updatedEmbeds = addAdditionalAttachments(embeds, filesName)

    return buildEmbeds(updatedEmbeds.ifEmpty { embeds })
}

private fun addAdditionalAttachments(embeds: MutableList<InlineEmbed>, files: List<String>): List<InlineEmbed> {
    files.forEach { file ->
        embeds.add(EmbedBuilder {
            url = embeds[0].url
            title = embeds[0].title
            image = "attachment://${file}" //if this doesn't work, be sure file doesn't have any schizo character that discord hates
        })
    }
    return embeds
}

private fun buildEmbeds(embeds: List<InlineEmbed>): List<MessageEmbed> = embeds.map { it.build() }

private fun hasVideos(files: List<String>): Boolean =
    files.any { path -> setOf(".mp4", ".webm", ".mov").any { extension -> path.endsWith(extension, ignoreCase = true) } }

private data class EmbedInfo(
    val nick: String,
    val username: String,
    val avatar: String,
    val retweet: String,
    val favourite: String,
    val timestamp: String,
    val text: String? = null,
)