package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaCommandHandler
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaPeriodAutoComplete
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryResponsibilitiesForPeriodUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message.MAX_CONTENT_LENGTH
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class JdaResponsibilitiesForPeriodDiscordCommand(
    private val queryResponsibilitiesForPeriodUseCase: QueryResponsibilitiesForPeriodUseCase,
) : JdaCommandHandler {
    override val commandName: String = "responsibilities"

    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", false, true),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == "responsibilities") { "Invalid command for responsibilities" }

        val period = Period.parseOrNullIfBlank(interaction.getOption("start")?.asString)
            ?: Period.firstAfter(LocalDate.now())
        val responsibilities = queryResponsibilitiesForPeriodUseCase.responsibilitiesFor(
            ResponsibilitiesForPeriodQuery(period = period),
        )

        val message = ResponsibilitiesDiscordMessageFormatter.format(period, responsibilities)
        try {
            interaction.reply(message).setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply("Entrée invalide: ${e.message}")
                .setEphemeral(true)
                .queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to query responsibilities" }
            interaction.reply("Erreur interne lors de la récupération des responsabilités.")
                .setEphemeral(true)
                .queue()
        }
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == "responsibilities") { "Invalid command for responsibilities" }

        if (interaction.focusedOption.name != "start") {
            interaction.replyChoices(emptyList()).queue()
            return
        }

        interaction.replyChoices(JdaPeriodAutoComplete.nextPeriods()).queue()
    }
}

private object ResponsibilitiesDiscordMessageFormatter {

    fun format(period: Period, responsibilities: List<Responsibility>): String {
        if (responsibilities.isEmpty()) {
            return "Aucune responsabilité active pour la période du ${period.start}."
        }

        val header = "Responsabilités pour la période du ${period.start} :\n"
        val body = buildString {
            responsibilities.forEach { responsibility ->
                append("- ")
                append(responsibility.name)
                append('\n')
            }
        }

        val full = (header + body).trimEnd()
        if (full.length <= MAX_CONTENT_LENGTH) {
            return full
        }

        val suffix = "\n(liste tronquée)"
        val maxLen = (MAX_CONTENT_LENGTH - suffix.length).coerceAtLeast(0)
        return (full.take(maxLen).trimEnd() + suffix)
    }
}
