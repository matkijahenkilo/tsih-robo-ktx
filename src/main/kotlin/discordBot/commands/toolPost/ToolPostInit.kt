package discordBot.commands.toolPost

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun toolPosterInit(jda: JDA): SlashCommandData {
    //clear up folder before doing stuff
    workingDir.listFiles()?.forEach {
        if (it.name != ORIGINAL_VIDEO)
            it.delete()
    }

    jda.onCommand(ToolPostOptions.TOOLPOST) { event ->
        ToolPost().tryExecute(event)
    }

    return ToolPostOptions.getCommands()
}