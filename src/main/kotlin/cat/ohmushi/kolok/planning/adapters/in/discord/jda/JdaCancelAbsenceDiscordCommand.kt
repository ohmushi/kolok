package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.`in`.discord.kord.JdaCommandHandler
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
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
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Component
class JdaCancelAbsenceDiscordCommand(
    private val identityLinks: UserIdentityLinkRepository,
    private val cancelAbsence: CancelAbsenceUseCase,
    private val availabilityCalendarRepository: AvailabilityCalendarRepository,
) : JdaCommandHandler {
    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", true, true),
            OptionData(OptionType.USER, "absent", "Colocataire absent", false, false),
        )


    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == "cancel-absence") { "Invalid command for CancelAbsenceCommand" }

        val absent = interaction.getOption("absent")?.asUser ?: interaction.user
        val from = parsePeriod(interaction.getOption("start")?.asString?.trim().orEmpty())

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id)
            if (responsible == null) {
                interaction.reply("Aucun Responsable trouvé pour user=${absent}.").setEphemeral(true).queue()
                return
            }

            cancelAbsence.cancelAbsence(
                CancelAbsenceCommand(
                    responsible = responsible,
                    from = from,
                )
            )

            interaction.reply("Absence retirée pour ${responsible.name} à partir du ${from.start}.").setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply("Entrée invalide: ${e.message}").setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to cancel absence" }
            interaction.reply("Erreur interne lors de l'annulation.").setEphemeral(true).queue()
        }
    }

    private fun parsePeriod(date: String): Period {
        val parsed = LocalDate.parse(date)
        return Period(parsed)
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == "cancel-absence") { "Invalid command for CancelAbsenceCommand" }

        val absent = interaction.getOption("absent")?.asUser?.id ?: interaction.user.id

        val responsible = identityLinks.findResponsibleIdByUserId(userId = absent)
        if(responsible == null) {
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