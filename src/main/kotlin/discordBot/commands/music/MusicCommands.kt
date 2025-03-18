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
    const val MUSIC_OPTION_LINK = "link_ou_pesquisa"
    const val MUSIC_OPTION_SEARCH = "sites_de_pesquisa"
    const val MUSIC_RESUME_TRACK_LIST = "resumir_tracklist"
    const val MUSIC_SHOW_TRACK_LIST = "mostrar_tracklist"

    private const val ID_MUSIC = "music"
    const val PLAY = "$ID_MUSIC:play"
    const val STOP = "$ID_MUSIC:stop"
    const val SKIP = "$ID_MUSIC:skip"
    const val REPEAT = "$ID_MUSIC:repeat"
    const val SHUFFLE = "$ID_MUSIC:shuffle"

    fun getCommands(): SlashCommandData =
        Commands.slash(MUSIC, "Pesquiso ou toco músicas direto através de um link.").addSubcommands(
            Subcommand(MUSIC_PLAY, "Irei pesquisar ou tocar alguma música no chat de voz!").addOptions(
                OptionData(OptionType.STRING, MUSIC_OPTION_LINK, "Irei tocar uma música no chat!", true),
                OptionData(
                    OptionType.STRING,
                    MUSIC_OPTION_SEARCH,
                    "Força uma pesquisa em um site específico para a opção $MUSIC_PLAY.",
                    false
                ).addChoices(
                    Command.Choice("YouTube", "ytsearch:"),
                    // Command.Choice("Spotify", "spsearch:") // needs auth in yaml, not going to happen
                )
            ),
            Subcommand(MUSIC_RESUME_TRACK_LIST, "Resumo a tracklist salva desde a última vez que estive em call~"),
            Subcommand(MUSIC_SHOW_TRACK_LIST, "Mostro a tracklist atual~")
        )
}