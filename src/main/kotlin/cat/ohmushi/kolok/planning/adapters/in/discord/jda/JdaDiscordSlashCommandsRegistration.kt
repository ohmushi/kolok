package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.availabilities.JdaAddAbsenceDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.availabilities.JdaCancelAbsenceDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaAddResponsibilityDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaRemoveResponsibilityDiscordCommand
import cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities.JdaResponsibilitiesForPeriodDiscordCommand
import cat.ohmushi.kolok.planning.adapters.infrastructure.JdaDiscordConnexion
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class JdaDiscordSlashCommandsRegistration(
    private val jdaDiscordConnexion: JdaDiscordConnexion,
    addAbsenceDiscordCommand: JdaAddAbsenceDiscordCommand,
    cancelAbsenceDiscordCommand: JdaCancelAbsenceDiscordCommand,
    responsibilitiesForPeriodDiscordCommand: JdaResponsibilitiesForPeriodDiscordCommand,
    addResponsibilityDiscordCommand: JdaAddResponsibilityDiscordCommand,
    removeResponsibilityDiscordCommand: JdaRemoveResponsibilityDiscordCommand,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private data class JdaCommandGroupSpec(
        val name: String,
        val description: String,
        val subcommands: Map<String, JdaCommandHandler>,
    )

    private val commandGroups: List<JdaCommandGroupSpec> = listOf(
        JdaCommandGroupSpec(
            name = "absences",
            description = "Gestion des absences",
            subcommands = mapOf(
                "add" to addAbsenceDiscordCommand,
                "cancel" to cancelAbsenceDiscordCommand,
            ),
        ),
        JdaCommandGroupSpec(
            name = "responsibilities",
            description = "Gestion des responsabilités",
            subcommands = mapOf(
                "list" to responsibilitiesForPeriodDiscordCommand,
                "add" to addResponsibilityDiscordCommand,
                "remove" to removeResponsibilityDiscordCommand,
            ),
        ),
    )

    private val rootHandlers: Map<String, JdaCommandHandler> = commandGroups
        .associate { group ->
            group.name to JdaSubcommandGroupHandler(
                commandName = group.name,
                subcommands = group.subcommands,
            )
        }

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

        commandGroups.forEach { group ->
            commands.addCommands(
                Commands.slash(group.name, group.description)
                    .addSubcommands(
                        group.subcommands.map { (name, handler) ->
                            SubcommandData(name, name)
                                .addOptions(handler.options)
                        },
                    ),
            )
        }

        commands.queue(
            Consumer { logger.info("Commands registered [${rootHandlers.keys.joinToString(", ")}]") },
            Consumer { logger.error("Failed to register commands") },
        )
    }

    private fun registerHandlers(jda: JDA) {
        jda.listener<SlashCommandInteractionEvent> {
            val handler = rootHandlers[it.name]
            requireNotNull(handler) { "No handler for command ${it.name}" }
                .handle(it)
        }

        jda.listener<CommandAutoCompleteInteractionEvent> {
            val handler = rootHandlers[it.name]
            requireNotNull(handler) { "No handler for command ${it.name}" }
                .handle(it)
        }
    }
}
