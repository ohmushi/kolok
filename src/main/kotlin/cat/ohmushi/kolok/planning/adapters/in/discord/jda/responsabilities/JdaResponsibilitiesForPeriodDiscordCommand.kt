package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaCommandHandler
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaPeriodAutoComplete
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryNextResponsibilitiesVersionUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryResponsibilitiesForPeriodUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.domain.planning.Period
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class JdaResponsibilitiesForPeriodDiscordCommand(
    private val queryResponsibilitiesForPeriodUseCase: QueryResponsibilitiesForPeriodUseCase,
    private val queryNextResponsibilitiesVersionUseCase: QueryNextResponsibilitiesVersionUseCase,
) : JdaCommandHandler {
    override val commandName: String = "list"

    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", false, true),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        val period = Period.parseOrNullIfBlank(interaction.getOption("start")?.asString)
            ?: Period.firstAfter(LocalDate.now())
        val responsibilities = queryResponsibilitiesForPeriodUseCase.responsibilitiesFor(
            ResponsibilitiesForPeriodQuery(period = period),
        )
        val nextPeriod = queryNextResponsibilitiesVersionUseCase.nextVersionAfter(period)

        val message = ResponsibilitiesDiscordMessageFormatter.format(period, responsibilities, nextPeriod)
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
        if (interaction.focusedOption.name != "start") {
            interaction.replyChoices(emptyList()).queue()
            return
        }

        interaction.replyChoices(JdaPeriodAutoComplete.nextPeriods()).queue()
    }
}

