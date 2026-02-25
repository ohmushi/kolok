package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class JdaSubcommandGroupHandler(
    override val commandName: String,
    private val subcommands: List<JdaCommandHandler>,
) : JdaCommandHandler {

    override val options: List<OptionData>
        get() = emptyList()

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == commandName) { "Invalid command for $commandName" }

        val subcommandName = interaction.subcommandName
        require(!subcommandName.isNullOrBlank()) { "Missing subcommand for $commandName" }

        val handler = requireNotNull(subcommands.find { subcommand -> subcommand.commandName == subcommandName }) { "Unknown subcommand $commandName/$subcommandName" }
        handler.handle(interaction)
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == commandName) { "Invalid command for $commandName" }

        val subcommandName = interaction.subcommandName
        if (subcommandName.isNullOrBlank()) {
            interaction.replyChoices(emptyList()).queue()
            return
        }

        val handler = subcommands.find { subcommand -> subcommand.commandName == subcommandName }
        if (handler == null) {
            interaction.replyChoices(emptyList()).queue()
            return
        }

        handler.handle(interaction)
    }
}

