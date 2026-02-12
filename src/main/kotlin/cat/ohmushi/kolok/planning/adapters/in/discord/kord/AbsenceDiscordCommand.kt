package cat.ohmushi.kolok.planning.adapters.`in`.discord.kord

import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.cache.data.OptionData
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class AbsenceDiscordCommand(
    val identityLinks: UserIdentityLinkRepository,
    private val recordAbsence: RecordAbsenceUseCase,
) : KordCommandHandler {
    private val logger = KotlinLogging.logger {}


    override fun build(): ChatInputCreateBuilder.() -> Unit = {
        // TODO group into SubCommands
        // TODO commande peut ouvrir une modal
        string("start", "Début (YYYY-MM-DD, doit être un lundi)") {
            required = true
            autocomplete = true
        }
        integer("count", "Nombre de semaines d'absence, défaut=1") { required = false }
        user("absent", "Colocataire absent") { required = false }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {
        require(interaction.command.rootName == "absence") { "Invalid command for AbsenceCommandHandler" }

        val absent = interaction.command.users["absent"] ?: interaction.user
        val fromStr = interaction.command.strings["start"]?.trim().orEmpty()
        val periodsCount = interaction.command.integers["count"]?.toInt() ?: 1

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id.toString())
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

    override suspend fun handle(interaction: AutoCompleteInteraction) {
        require(interaction.command.rootName == "absence") { "Invalid command for AbsenceDiscordCommand" }

        val choices = (0..5).map {
            val nextPeriod =
                Period.firstAfter(LocalDate.now()).plus(it.toLong()).start.format(DateTimeFormatter.ISO_LOCAL_DATE)
            Choice.StringChoice(
                nextPeriod,
                value = nextPeriod,
                nameLocalizations = Optional.Missing(),
            )
        }

        interaction.suggest(choices)
    }
}