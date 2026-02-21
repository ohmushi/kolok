package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

interface JdaCommandHandler {
    val commandName: String
    val options: List<OptionData>

    suspend fun handle(interaction: SlashCommandInteractionEvent)
    suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        // default no-op
    }
}