package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.`in`.discord.kord.JdaCommandHandler
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
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
import java.time.format.DateTimeFormatter

@Component
class JdaAbsenceDiscordCommand(
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

        val from = parsePeriod(interaction.getOption("start")?.asString?.trim().orEmpty())
        val periodsCount = interaction.getOption("count")?.asInt ?: 1
        val absent = interaction.getOption("absent")?.asUser ?: interaction.user

        try {
            val responsible = identityLinks.findResponsibleIdByUserId(userId = absent.id)
            if (responsible == null) {
                interaction.reply("Aucun Responsable trouvé pour user=${absent}.").setEphemeral(true).queue()
                return
            }

            recordAbsence.recordAbsence(
                RecordAbsenceCommand(
                    responsible = responsible,
                    from = from,
                    periodsCount = periodsCount,
                )
            )

            interaction
                .reply("Absence enregistrée pour ${responsible.name} à partir du ${from.start} (durée: ${periodsCount} période(s)).")
                .setEphemeral(true)
                .queue()
        } catch (e: IllegalArgumentException) {
            interaction.reply("Entrée invalide: ${e.message}").setEphemeral(true).queue()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to record absence" }
            interaction.reply("Erreur interne lors de l'enregistrement.").setEphemeral(true).queue()
        }
    }

    private fun parsePeriod(date: String): Period {
        val parsed = LocalDate.parse(date)
        return Period(parsed)
    }

    override suspend fun handle(interaction: CommandAutoCompleteInteractionEvent) {
        require(interaction.name == "absence") { "Invalid command for AbsenceDiscordCommand" }

        val choices = (0..5).map {
            val nextPeriod =
                Period.firstAfter(LocalDate.now()).plus(it.toLong()).start.format(DateTimeFormatter.ISO_LOCAL_DATE)
            Command.Choice(nextPeriod, nextPeriod)
        }

        interaction.replyChoices(choices).queue()
    }
}