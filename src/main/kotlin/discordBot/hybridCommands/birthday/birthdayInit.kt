package discordBot.hybridCommands.birthday

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.matkija.bot.discordBot.hybridCommands.birthday.BirthdaySlashCommands

fun birthdayInit(jda: JDA): SlashCommandData {
    jda.onCommand(BirthdaySlashCommands.BIRTHDAY) { event ->
        if (event.guild == null)
            event.reply("This only work in servers nanora!").queue()
        Birthday(event).tryExecute()
    }

    return BirthdaySlashCommands.getCommands()
}