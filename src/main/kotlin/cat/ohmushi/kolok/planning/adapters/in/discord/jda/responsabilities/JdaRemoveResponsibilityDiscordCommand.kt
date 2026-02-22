package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaCommandHandler
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaPeriodAutoComplete
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.RemoveResponsibilityCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.RemoveResponsibilityUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryResponsibilitiesForPeriodUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class JdaRemoveResponsibilityDiscordCommand(
    private val removeResponsibility: RemoveResponsibilityUseCase,
    private val queryResponsibilitiesForPeriodUseCase: QueryResponsibilitiesForPeriodUseCase,
) : JdaCommandHandler {

    override val commandName: String = "remove"

    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "name", "Nom de la responsabilité", true, true),
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", false, true),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == commandName) { "Invalid command for $commandName" }

        val message = run {
            val name = interaction.getOption("name")?.asString?.trim().orEmpty()
            require(name.isNotBlank()) { "name ne doit pas être vide" }

            val period = Period.parseOrNullIfBlank(interaction.getOption("start")?.asString)
                ?: Period.firstAfter(LocalDate.now())

            removeResponsibility.removeResponsibility(
                RemoveResponsibilityCommand(
                    responsibility = Responsibility(name),
                    from = period,
                )
            )

            RemoveResponsibilityMessageFormatter.removed(name = name, from = period)
        }

        try {
            interaction.reply(message).setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply(RemoveResponsibilityMessageFormatter.invalidInput(e.message)).setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to remove responsibility" }
            interaction.reply(RemoveResponsibilityMessageFormatter.internalError()).setEphemeral(true).queue()
        }
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        logger.info { interaction.focusedOption.value }
        require(interaction.name == commandName) { "Invalid command for $commandName" }

        when (interaction.focusedOption.name) {
            "start" -> interaction.replyChoices(JdaPeriodAutoComplete.nextPeriods()).queue()
            "name" -> {
                val period = Period.parseOrNullIfBlank(interaction.getOption("start")?.asString)
                    ?: Period.firstAfter(LocalDate.now())

                val known = queryResponsibilitiesForPeriodUseCase.responsibilitiesFor(
                    ResponsibilitiesForPeriodQuery(period = period)
                )

                val typed = interaction.focusedOption.value.trim()
                val matches = if (typed.isBlank()) {
                    known
                } else {
                    known.filter { it.name.contains(typed, ignoreCase = true) }
                }

                interaction.replyChoices(
                    matches.take(10).map { r -> Command.Choice(r.name, r.name) }
                ).queue()
            }
            else -> interaction.replyChoices(emptyList()).queue()
        }
    }
}

private object RemoveResponsibilityMessageFormatter {

    fun removed(name: String, from: Period): String {
        return "Responsabilité retirée: $name (à partir du ${from.start})."
    }

    fun invalidInput(details: String?): String {
        return if (details.isNullOrBlank()) {
            "Entrée invalide."
        } else {
            "Entrée invalide: ${details}"
        }
    }

    fun internalError(): String {
        return "Erreur interne lors du retrait de la responsabilité."
    }
}
