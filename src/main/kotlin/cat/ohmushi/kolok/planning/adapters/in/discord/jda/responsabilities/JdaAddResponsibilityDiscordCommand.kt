package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaCommandHandler
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaPeriodAutoComplete
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.AddResponsibilityCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.AddResponsibilityUseCase
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class JdaAddResponsibilityDiscordCommand(
    private val addResponsibility: AddResponsibilityUseCase,
) : JdaCommandHandler {

    override val commandName: String = "add-responsibility"

    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "name", "Nom de la responsabilité", true, false),
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", false, true),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == "add-responsibility") { "Invalid command for add-responsibility" }

        val message = run {
            val name = interaction.getOption("name")?.asString?.trim().orEmpty()
            require(name.isNotBlank()) { "name ne doit pas être vide" }

            val period = Period.parseOrNullIfBlank(interaction.getOption("start")?.asString)
                ?: Period.firstAfter(LocalDate.now())

            addResponsibility.addResponsibility(
                AddResponsibilityCommand(
                    responsibility = Responsibility(name),
                    from = period,
                )
            )

            AddResponsibilityMessageFormatter.added(name = name, from = period)
        }

        try {
            interaction.reply(message).setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply(AddResponsibilityMessageFormatter.invalidInput(e.message)).setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to add responsibility" }
            interaction.reply(AddResponsibilityMessageFormatter.internalError()).setEphemeral(true).queue()
        }
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == "add-responsibility") { "Invalid command for add-responsibility" }

        if (interaction.focusedOption.name != "start") {
            interaction.replyChoices(emptyList()).queue()
            return
        }

        interaction.replyChoices(JdaPeriodAutoComplete.nextPeriods()).queue()
    }
}

private object AddResponsibilityMessageFormatter {

    fun added(name: String, from: Period): String {
        return "Responsabilité ajoutée: $name (à partir du ${from.start})."
    }

    fun invalidInput(details: String?): String {
        return if (details.isNullOrBlank()) {
            "Entrée invalide."
        } else {
            "Entrée invalide: ${details}"
        }
    }

    fun internalError(): String {
        return "Erreur interne lors de l'ajout de la responsabilité."
    }
}
