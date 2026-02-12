package cat.ohmushi.kolok.planning.adapters.`in`.discord.kord

import cat.ohmushi.kolok.planning.adapters.infrastructure.KordDiscordConnexion
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DiscordSlashCommandsAdapter(
    private val kordDiscordConnexion: KordDiscordConnexion,
    absenceDiscordCommand: AbsenceDiscordCommand,
    cancelAbsenceDiscordCommand: CancelAbsenceDiscordCommand,
    @Value("\${discord.guild-id:}") private val guildId: String? = null,
) {
    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val handlers: Map<String, KordCommandHandler> = mapOf(
        "absence" to absenceDiscordCommand,
        "cancel-absence" to cancelAbsenceDiscordCommand,
    )

    @EventListener(ApplicationReadyEvent::class)
    fun registerSlashCommands() {
        scope.launch {
            kordDiscordConnexion.withKord { kord ->
                registerCommands(kord)
                registerHandlers(kord)
                logger.info { "Discord slash commands registered/handled." }
            }
        }
    }

    private suspend fun registerCommands(kord: Kord) {
        val gid = Snowflake(requireNotNull(guildId) { "Discord guild ID must be set to register slash commands" })
        handlers.forEach { (commandName, command) ->
            scope.launch {
                // TODO go to global
                kord.createGuildChatInputCommand(gid, commandName, commandName, command.build())
            }
        }
    }

    private fun registerHandlers(kord: Kord) {
        kord.on<ChatInputCommandInteractionCreateEvent> {
            val handler = handlers[interaction.command.rootName]
            requireNotNull(handler)
                { "No handler for command ${interaction.command.rootName}" }
                .handle(interaction)
        }

        kord.on<AutoCompleteInteractionCreateEvent> {
            val handler = handlers[interaction.command.rootName]
            requireNotNull(handler)
            { "No handler for command ${interaction.command.rootName}" }.handle(interaction)
        }
    }
}

interface KordCommandHandler {
    suspend fun handle(interaction: ChatInputCommandInteraction)
    suspend fun handle(interaction: AutoCompleteInteraction) {
        // default no-op
    }
    fun build(): ChatInputCreateBuilder.() -> Unit
}
