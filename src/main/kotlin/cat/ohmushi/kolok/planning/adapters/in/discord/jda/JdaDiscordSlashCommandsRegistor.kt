package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.infrastructure.JdaDiscordConnexion
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class JdaDiscordSlashCommandsRegistor(
    private val jdaDiscordConnexion: JdaDiscordConnexion,
    absenceDiscordCommand: JdaAddAbsenceDiscordCommand,
    cancelAbsenceDiscordCommand: JdaCancelAbsenceDiscordCommand,
    responsibilitiesForPeriodDiscordCommand: JdaResponsibilitiesForPeriodDiscordCommand,
    addResponsibilityDiscordCommand: JdaAddResponsibilityDiscordCommand,
    removeResponsibilityDiscordCommand: JdaRemoveResponsibilityDiscordCommand,
) {
    private val logger by SLF4J
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // TODO reflection to avoid hardcoding command names here
    // TODO make subcommands (e.g. /absence record, /absence cancel) to avoid hardcoding command names here
    // TODO add name directly in the command handler and use it here to avoid hardcoding command names here
    private val handlers: Map<String, JdaCommandHandler> = mapOf(
        // availabilities
        "absence" to absenceDiscordCommand,
        "cancel-absence" to cancelAbsenceDiscordCommand,

        // responsibilities
        "responsibilities" to responsibilitiesForPeriodDiscordCommand,
        "remove-responsibility" to removeResponsibilityDiscordCommand,
        "add-responsibility" to addResponsibilityDiscordCommand,
    )

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
        handlers.forEach { (commandName, command) ->
            commands.addCommands(
                Commands.slash(commandName, commandName).addOptions(command.options),
            )
        }
        commands.queue(
            Consumer { logger.info("Commands registered [${handlers.keys.joinToString(", ")}]") },
            Consumer { logger.error("Failed to register commands") })
    }


    private fun registerHandlers(jda: JDA) {
        jda.listener<SlashCommandInteractionEvent> {

            val handler = handlers[it.name]
            requireNotNull(handler)
            { "No handler for command ${it.name}" }
                .handle(it)

        }

        jda.listener<CommandAutoCompleteInteractionEvent> {
            val handler = handlers[it.name]
            requireNotNull(handler)
            { "No handler for command ${it.name}" }.handle(it)
        }
    }
}
