package cat.ohmushi.kolok.planning.adapters.`in`.discord

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordConnexion
import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordId
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.DiscordIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.Period
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DiscordSlashCommandsAdapter(
    private val discordConnexion: DiscordConnexion,
    private val recordAbsence: RecordAbsenceUseCase,
    private val identityLinks: DiscordIdentityLinkRepository,
    @Value("\${discord.guild-id:}") private val guildId: String? = null,
) {

    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @EventListener(ApplicationReadyEvent::class)
    fun registerSlashCommands() {
        scope.launch {
            discordConnexion.withKord { kord ->
                registerCommands(kord)
                registerHandlers(kord)
                logger.info { "Discord slash commands registered/handled." }
            }
        }
    }

    private suspend fun registerCommands(kord: Kord) {
        val gid = guildId?.takeIf { it.isNotBlank() }?.let { Snowflake(it) }

        if (gid != null) {
            kord.createGuildChatInputCommand(
                gid,
                "absence",
                "Enregistre une absence"
            ) {
                string("absent", "Mention @ ou snowflake de l'utilisateur absent") { required = true }
                string("start", "Début (YYYY-MM-DD, doit être un lundi)") { required = true }
                string("end", "Fin (YYYY-MM-DD, doit être un lundi)") { required = false }
            }
        } else {
            kord.createGlobalChatInputCommand("absence", "Enregistre une absence") {
                string("absent", "Mention @ ou snowflake de l'utilisateur absent") { required = true }
                string("start", "Début (YYYY-MM-DD, doit être un lundi)") { required = true }
                string("end", "Fin (YYYY-MM-DD, doit être un lundi)") { required = false }
            }
        }
    }

    private fun registerHandlers(kord: Kord) {
        kord.on<ChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != "absence") return@on

            val absentRaw = interaction.command.strings["absent"]?.trim().orEmpty()
            val fromStr = interaction.command.strings["start"]?.trim().orEmpty()
            val toStr = interaction.command.strings["end"]?.trim()

            try {
                val discordId = DiscordId.parse(absentRaw)

                val responsible = identityLinks.findResponsibleIdByDiscordUserId(discordId.snowflake)
                if (responsible == null) {
                    interaction.respondEphemeral {
                        content = "Aucun Responsable mappé pour discordUserId=${discordId.snowflake}."
                    }
                    return@on
                }

                val from = parsePeriod(fromStr)
                val to = if (toStr != null) parsePeriod(toStr) else from

                recordAbsence.recordAbsence(
                    RecordAbsenceCommand(
                        responsible = responsible,
                        from = from,
                        toIncluded = to
                    )
                )

                interaction.respondEphemeral {
                    content = "Absence enregistrée pour ${responsible.name} du ${from.start} au ${to.start}."
                }
            } catch (e: IllegalArgumentException) {
                interaction.respondEphemeral { content = "Entrée invalide: ${e.message}" }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to record absence" }
                interaction.respondEphemeral { content = "Erreur interne lors de l'enregistrement." }
            }
        }
    }

    private fun parsePeriod(date: String): Period {
        val parsed = LocalDate.parse(date)
        return Period(parsed)
    }
}
