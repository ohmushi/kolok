package cat.ohmushi.kolok.planning.adapters.`in`.discord

import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import dev.kord.common.entity.Choice
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Component
class CancelAbsenceDiscordCommand(
    private val identityLinks: UserIdentityLinkRepository,
    private val cancelAbsence: CancelAbsenceUseCase,
    private val availabilityCalendarRepository: AvailabilityCalendarRepository,
) : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun build(): ChatInputCreateBuilder.() -> Unit = {
        string("start", "Début (YYYY-MM-DD, doit être un lundi)") {
            required = true
            autocomplete = true
        }
        user("absent", "Colocataire absent") {
            required = false
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {
        require(interaction.command.rootName == "cancel-absence") { "Invalid command for CancelAbsenceCommand" }

        val absent = interaction.command.users["absent"] ?: interaction.user
        val fromStr = interaction.command.strings["start"]?.trim().orEmpty()

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id.toString())
            if (responsible == null) {
                interaction.respondEphemeral {
                    content = "Aucun Responsable trouvé pour user=${absent}."
                }
                return
            }

            val from = parsePeriod(fromStr)

            cancelAbsence.cancelAbsence(
                CancelAbsenceCommand(
                    responsible = responsible,
                    from = from,
                )
            )

            interaction.respondEphemeral {
                content = "Absence retirée pour ${responsible.name} à partir du ${from.start}."
            }
        } catch (e: IllegalArgumentException) {
            interaction.respondEphemeral { content = "Entrée invalide: ${e.message}" }
        } catch (t: Throwable) {
            logger.error(t) { "Failed to cancel absence" }
            interaction.respondEphemeral { content = "Erreur interne lors de l'annulation." }
        }
    }

    private fun parsePeriod(date: String): Period {
        val parsed = LocalDate.parse(date)
        return Period(parsed)
    }

    override suspend fun handle(interaction: AutoCompleteInteraction) {
        require(interaction.command.rootName == "cancel-absence") { "Invalid command for CancelAbsenceCommand" }

        val absent = interaction.command.options["absent"]?.value as Snowflake? ?: interaction.user.id

        val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.toString())
        if(responsible == null) {
            logger.info { "No Responsible found for user $absent" }
            interaction.suggest(emptyList())
            return
        }

        val absences: List<String> =
            availabilityCalendarRepository.get().absencesOfResponsibleSince(responsible, Period.firstAfter(now())).map {
                it.from.start.format(
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }

        interaction.suggest(absences.map {
            Choice.StringChoice(
                name = it,
                value = it,
                nameLocalizations = Optional.Missing(),
            )
        })
    }
}