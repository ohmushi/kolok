package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.availabilities.JdaAddAbsenceDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.availabilities.JdaCancelAbsenceDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaAddResponsibilityDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaRemoveResponsibilityDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaResponsibilitiesForPeriodDiscordCommand
import cat.ohmushi.kolok.planning.adapters.infrastructure.JdaDiscordConnexion
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class JdaDiscordSlashCommandsRegistration(
    private val jdaDiscordConnexion: JdaDiscordConnexion,
    absenceDiscordCommand: JdaAddAbsenceDiscordCommand,
    cancelAbsenceDiscordCommand: JdaCancelAbsenceDiscordCommand,
    responsibilitiesForPeriodDiscordCommand: JdaResponsibilitiesForPeriodDiscordCommand,
    addResponsibilityDiscordCommand: JdaAddResponsibilityDiscordCommand,
    removeResponsibilityDiscordCommand: JdaRemoveResponsibilityDiscordCommand,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = kotlinx.coroutines.CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // TODO reflection to avoid hardcoding command names here
    // TODO make subcommands (e.g. /absence record, /absence cancel) to avoid hardcoding command names here
    private val handlers: Map<String, JdaCommandHandler> = listOf(
        absenceDiscordCommand,
        cancelAbsenceDiscordCommand,
        responsibilitiesForPeriodDiscordCommand,
        removeResponsibilityDiscordCommand,
        addResponsibilityDiscordCommand,
    ).associateBy { it.commandName }

    @EventListener(ApplicationReadyEvent::class)
    fun registerSlashCommands() {
        scope.launch {
            jdaDiscordConnexion.withJda { jda ->
                registerCommands(jda)
                registerHandlers(jda)
            }
        }
    }

    private fun registerCommands(jda: JDA) {
        val commands: CommandListUpdateAction = jda.updateCommands()
        handlers.values.forEach { handler ->
            commands.addCommands(
                Commands.slash(handler.commandName, handler.commandName).addOptions(handler.options),
            )
        }
        commands.queue(
            Consumer { logger.info("Commands registered [${handlers.keys.joinToString(", ")}]") },
            Consumer { logger.error("Failed to register commands") })
    }


    private fun registerHandlers(jda: JDA) {
        jda.listener<SlashCommandInteractionEvent> {
            val handler = handlers[it.name]
            requireNotNull(handler) { "No handler for command ${it.name}" }
                .handle(it)
        }

        jda.listener<CommandAutoCompleteInteractionEvent> {
            val handler = handlers[it.name]
            requireNotNull(handler) { "No handler for command ${it.name}" }
                .handle(it)
        }
    }
}
