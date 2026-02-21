package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component

@Component
class JdaAddAbsenceDiscordCommand(
    val identityLinks: UserIdentityLinkRepository,
    private val recordAbsence: RecordAbsenceUseCase,
) : JdaCommandHandler {
    private val logger = KotlinLogging.logger {}

    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "start", "Début (YYYY-MM-DD, doit être un lundi)", true, true),
            OptionData(OptionType.INTEGER, "count", "Nombre de semaines d'absence, défaut=1", false, false),
            OptionData(OptionType.USER, "absent", "Colocataire absent", false, false),
        )

    override suspend fun handle(interaction: SlashCommandInteractionEvent) {
        require(interaction.name == "absence") { "Invalid command for AbsenceCommandHandler" }

        val absent = interaction.getOption("absent")?.asUser ?: interaction.user
        val message = run {
            val from = Period.parse(interaction.getOption("start")?.asString?.trim().orEmpty())
            val periodsCount = interaction.getOption("count")?.asInt ?: 1

            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id)
            if (responsible == null) {
                JdaAbsenceMessageFormatter.noResponsibleFound(userTag = absent.asTag, userId = absent.id)
            } else {
                recordAbsence.recordAbsence(
                    RecordAbsenceCommand(
                        responsible = responsible,
                        from = from,
                        periodsCount = periodsCount,
                    )
                )

                JdaAbsenceMessageFormatter.recorded(
                    responsible = responsible,
                    from = from,
                    periodsCount = periodsCount,
                )
            }
        }

        try {
            interaction.reply(message).setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply(JdaAbsenceMessageFormatter.invalidInput(e.message)).setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to record absence" }
            interaction.reply(JdaAbsenceMessageFormatter.internalError()).setEphemeral(true).queue()
        }
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == "absence") { "Invalid command for AbsenceDiscordCommand" }

        interaction.replyChoices(JdaPeriodAutoComplete.nextPeriods()).queue()
    }
}

private object JdaAbsenceMessageFormatter {
    fun noResponsibleFound(userTag: String, userId: String): String {
        return "Aucun Responsable trouvé pour user=${userTag} (${userId})."
    }

    fun recorded(responsible: Responsible, from: Period, periodsCount: Int): String {
        return "Absence enregistrée pour ${responsible.name} à partir du ${from.start} (durée: ${periodsCount} période(s))."
    }

    fun invalidInput(details: String?): String {
        return if (details.isNullOrBlank()) {
            "Entrée invalide."
        } else {
            "Entrée invalide: ${details}"
        }
    }

    fun internalError(): String {
        return "Erreur interne lors de l'enregistrement."
    }
}
