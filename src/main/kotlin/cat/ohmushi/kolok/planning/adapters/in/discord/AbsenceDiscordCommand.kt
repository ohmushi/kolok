package cat.ohmushi.kolok.planning.adapters.`in`.discord

import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AbsenceDiscordCommand(
    val identityLinks: UserIdentityLinkRepository,
    private val recordAbsence: RecordAbsenceUseCase,
) : CommandHandler {
    private val logger = KotlinLogging.logger {}


    override fun build(): ChatInputCreateBuilder.() -> Unit = {
        // TODO group into SubCommands
        // TODO commande peut ouvrir une modal
        string("start", "Début (YYYY-MM-DD, doit être un lundi)") { required = true }
        integer("count", "Nombre de semaines d'absence, défaut=1") { required = false }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {
        require(interaction.command.rootName == "absence") { "Invalid command for AbscenceCommandHandler" }

        val absent = interaction.user.id.toString()
        val fromStr = interaction.command.strings["start"]?.trim().orEmpty()
        val periodsCount = interaction.command.integers["count"]?.toInt() ?: 1

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent)
            if (responsible == null) {
                interaction.respondEphemeral {
                    content = "Aucun Responsable trouvé pour user=${absent}."
                }
                return
            }

            val from = parsePeriod(fromStr)

            recordAbsence.recordAbsence(
                RecordAbsenceCommand(
                    responsible = responsible,
                    from = from,
                    periodsCount = periodsCount,
                )
            )

            interaction.respondEphemeral {
                content =
                    "Absence enregistrée pour ${responsible.name} à partir du ${from.start} (durée: ${periodsCount} période(s))."
            }
        } catch (e: IllegalArgumentException) {
            interaction.respondEphemeral { content = "Entrée invalide: ${e.message}" }
        } catch (t: Throwable) {
            logger.error(t) { "Failed to record absence" }
            interaction.respondEphemeral { content = "Erreur interne lors de l'enregistrement." }
        }
    }

    private fun parsePeriod(date: String): Period {
        val parsed = LocalDate.parse(date)
        return Period(parsed)
    }

}