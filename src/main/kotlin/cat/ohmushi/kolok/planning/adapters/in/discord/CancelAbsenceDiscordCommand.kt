package cat.ohmushi.kolok.planning.adapters.`in`.discord

import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CancelAbsenceDiscordCommand(
    val identityLinks: UserIdentityLinkRepository,
    private val cancelAbsence: CancelAbsenceUseCase,
) : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun build(): ChatInputCreateBuilder.() -> Unit = {
        string("start", "Début (YYYY-MM-DD, doit être un lundi)") { required = true }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {
        // TODO autoComplession avec Autocomplete => chercher en db toute les dates et les proposer
        require(interaction.command.rootName == "cancel-absence") { "Invalid command for CancelAbsenceCommand" }

        val absent = interaction.user.id.toString()
        val fromStr = interaction.command.strings["start"]?.trim().orEmpty()

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent)
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
                content =
                    "Absence retirée pour ${responsible.name} à partir du ${from.start}."
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
}