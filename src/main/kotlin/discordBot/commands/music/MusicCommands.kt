package org.matkija.bot.discordBot.commands.music

import dev.minn.jda.ktx.interactions.commands.Subcommand
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData


object MusicCommands {

    const val MUSIC = "music"
    const val MUSIC_PLAY = "play"

    //TODO: make a playnext command to be placed in first position of the list, regardless of shuffle
    const val MUSIC_OPTION_LINK = "url_or_search"
    const val MUSIC_OPTION_SEARCH = "website_for_search"
    const val MUSIC_RESUME_TRACK_LIST = "resume_tracklist"
    const val MUSIC_SHOW_TRACK_LIST = "show_tracklist"

    private const val ID_MUSIC = "music"
    const val PLAY = "$ID_MUSIC:play"
    const val STOP = "$ID_MUSIC:stop"
    const val SKIP = "$ID_MUSIC:skip"
    const val REPEAT = "$ID_MUSIC:repeat"
    const val SHUFFLE = "$ID_MUSIC:shuffle"

    fun getCommands(): SlashCommandData =
        Commands.slash(MUSIC, "I search or play songs directly from a link nanora!").addSubcommands(
            Subcommand(MUSIC_PLAY, "I'll search or play something in voice chat nora!").addOptions(
                OptionData(OptionType.STRING, MUSIC_OPTION_LINK, "I'll play a song in voice chat!", true),
                OptionData(
                    OptionType.STRING,
                    MUSIC_OPTION_SEARCH,
                    "Forces a search in a specific website for the option $MUSIC_PLAY.",
                    false
                ).addChoices(
                    Command.Choice("YouTube", "ytsearch:"),
                    // Command.Choice("Spotify", "spsearch:") // needs auth in yaml
                )
            ),
            Subcommand(
                MUSIC_RESUME_TRACK_LIST,
                "Resumes a saved playlist from the last time I've been in voice chat nora~"
            ),
            Subcommand(MUSIC_SHOW_TRACK_LIST, "I show the current tracklist nora~")
        )
}