package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.availabilities

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.JdaCommandHandler
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Component
class JdaCancelAbsenceDiscordCommand(
    private val identityLinks: UserIdentityLinkRepository,
    private val cancelAbsence: CancelAbsenceUseCase,
    private val availabilityCalendarRepository: AvailabilityCalendarRepository,
) : JdaCommandHandler {
    override val commandName: String = "cancel"

    private val logger = KotlinLogging.logger {}

    private object CancelAbsenceMessageFormatter {
        fun noResponsibleFound(user: String): String = "Aucun Responsable trouvé pour user=$user."

        fun cancelled(responsibleName: String, from: Period): String =
            "Absence retirée pour $responsibleName à partir du ${from.start}."

        fun invalidInput(details: String?): String = "Entrée invalide: ${details ?: "(détails indisponibles)"}"

        fun internalError(): String = "Erreur interne lors de l'annulation."
    }

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", true, true),
            OptionData(OptionType.USER, "absent", "Colocataire absent", false, false),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        val absent = interaction.getOption("absent")?.asUser ?: interaction.user
        val from = Period.parse(interaction.getOption("start")?.asString?.trim().orEmpty())
        val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id)
        if (responsible == null) {
            interaction.reply(CancelAbsenceMessageFormatter.noResponsibleFound(absent.toString())).setEphemeral(true).queue()
            return
        }
        cancelAbsence.cancelAbsence(
            CancelAbsenceCommand(
                responsible = responsible,
                from = from,
            )
        )
        try {
            interaction.reply(CancelAbsenceMessageFormatter.cancelled(responsible.name, from)).setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply(CancelAbsenceMessageFormatter.invalidInput(e.message)).setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to cancel absence" }
            interaction.reply(CancelAbsenceMessageFormatter.internalError()).setEphemeral(true).queue()
        }
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        val absent = interaction.getOption("absent")?.asUser?.id ?: interaction.user.id

        val responsible = identityLinks.findResponsibleIdByUserId(userId = absent)
        if (responsible == null) {
            logger.info { "No Responsible found for user $absent" }
            interaction.replyChoices(emptyList()).queue()
            return
        }

        val absences: List<String> =
            availabilityCalendarRepository.get().absencesOfResponsibleSince(responsible, Period.firstAfter(now())).map {
                it.from.start.format(
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }

        interaction.replyChoices(absences.map {
            Command.Choice(it, it)
        }).queue()
    }
}