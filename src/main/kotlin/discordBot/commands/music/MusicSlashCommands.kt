package org.matkija.bot.discordBot.commands.music

import dev.minn.jda.ktx.interactions.commands.Subcommand
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData


object MusicSlashCommands {

    const val MUSIC = "music"
    const val MUSIC_PLAY = "play"
    const val MUSIC_PLAY_NEXT = "play_next"

    const val MUSIC_OPTION_LINK = "url_or_search"
    const val MUSIC_OPTION_SEARCH = "website_for_search"
    const val MUSIC_RESUME_QUEUE = "resume_queue"
    const val MUSIC_SHOW_QUEUE = "show_queue"

    private const val ID_MUSIC = "music"
    const val PLAY = "$ID_MUSIC:play"
    const val STOP = "$ID_MUSIC:stop"
    const val SKIP = "$ID_MUSIC:skip"
    const val REPEAT = "$ID_MUSIC:repeat"
    const val SHUFFLE = "$ID_MUSIC:shuffle"

    private fun linkInputOptionData(): OptionData = OptionData(
        OptionType.STRING,
        MUSIC_OPTION_SEARCH,
        "Forces a search in a specific website for the option $MUSIC_PLAY.",
        false
    ).addChoices(
        Command.Choice("YouTube", "ytsearch:"),
        // Command.Choice("Spotify", "spsearch:") // needs auth in yaml
    )

    fun getCommands(): SlashCommandData =
        Commands.slash(MUSIC, "I search or play songs directly from a link nanora!").addSubcommands(
            Subcommand(MUSIC_PLAY, "I'll search or play something in voice chat nora!").addOptions(
                OptionData(OptionType.STRING, MUSIC_OPTION_LINK, "I'll play a song in voice chat!", true),
                linkInputOptionData()
            ),
            Subcommand(MUSIC_PLAY_NEXT, "Prioritizes a track to be played next nora~").addOptions(
                OptionData(
                    OptionType.STRING,
                    MUSIC_OPTION_LINK,
                    "I'll prioritize this song to play next nanora!",
                    true
                ),
                linkInputOptionData()
            ),
            Subcommand(
                MUSIC_RESUME_QUEUE,
                "Resumes a saved playlist from the last time I've been in voice chat nora~"
            ),
            Subcommand(MUSIC_SHOW_QUEUE, "I show the current queue nora~")
        )
}