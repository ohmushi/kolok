package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.infrastructure.JdaDiscordConnexion
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class JdaDiscordSlashCommandsAdapter(
    private val jdaDiscordConnexion: JdaDiscordConnexion,
    absenceDiscordCommand: JdaAbsenceDiscordCommand,
    cancelAbsenceDiscordCommand: JdaCancelAbsenceDiscordCommand,
    responsibilitiesForPeriodDiscordCommand: JdaResponsibilitiesForPeriodDiscordCommand,
) {
    private val logger by SLF4J
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // TODO reflection to avoid hardcoding command names here
    // TODO make subcommands (e.g. /absence record, /absence cancel) to avoid hardcoding command names here
    private val handlers: Map<String, JdaCommandHandler> = mapOf(
        "absence" to absenceDiscordCommand,
        "cancel-absence" to cancelAbsenceDiscordCommand,
        "responsibilities" to responsibilitiesForPeriodDiscordCommand,
    )

    @EventListener(ApplicationReadyEvent::class)
    fun registerSlashCommands() {
        scope.launch {
            jdaDiscordConnexion.withJda { jda ->
                registerCommands(jda)
                registerHandlers(jda)

                logger.info("Discord slash commands registered/handled.")
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
        commands.queue()
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
