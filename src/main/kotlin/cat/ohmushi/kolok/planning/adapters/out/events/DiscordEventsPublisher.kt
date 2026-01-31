package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordConnexion
import cat.ohmushi.kolok.planning.adapters.infrastructure.User
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.events.EventHandler
import cat.ohmushi.kolok.planning.domain.events.PlanningGenerated
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class DiscordEventsPublisher(
    private val discordConnexion: DiscordConnexion,
    @Value("\${discord.planning.channel}") private val channelId: String,
    private val plannings: PlanningRepository,
    private val availabilities: AvailabilityCalendarRepository,
    private val formatter: PlanningMessageFormatter,
    private val identityLinks: UserIdentityLinkRepository,
) : EventsPublisher {

    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @Volatile
    private var kord: Kord? = null

    @Volatile
    private var channel: MessageChannel? = null

    @Suppress("UNCHECKED_CAST")
    private fun handlers(): Map<KClass<out DomainEvent>, EventHandler<DomainEvent>> {
        val ch = channel ?: return emptyMap()
        return mapOf(
            PlanningGenerated::class as KClass<DomainEvent> to PlanningGeneratedHandler(
                plannings = plannings,
                availabilities = availabilities,
                formatter = formatter,
                identityLinks = identityLinks,
                channel = ch,
            ) as EventHandler<DomainEvent>,
        )
    }

    @EventListener(ApplicationReadyEvent::class)
    fun warmupKordAndChannel() {
        scope.launch {
            try {
                discordConnexion.withKord { k ->
                    kord = k
                    channel = requireNotNull(
                        k.getChannelOf<MessageChannel>(Snowflake(channelId))
                    ) { "Channel not found (id=$channelId)" }
                }
                logger.info { "DiscordEventsPublisher ready (channelId=$channelId)." }
            } catch (t: Throwable) {
                logger.error(t) { "DiscordEventsPublisher warmup failed; events won't be published to Discord." }
                kord = null
                channel = null
            }
        }
    }

    override fun publish(events: List<DomainEvent>) {
        scope.launch {
            val handlers = handlers()
            if (handlers.isEmpty()) {
                logger.debug { "Discord not ready; skipping ${events.size} event(s)." }
                return@launch
            }

            for (event in events) {
                try {
                    handlers[event::class]?.handle(event)
                } catch (t: Throwable) {
                    logger.error(t) { "Failed to publish event ${event::class.simpleName} to Discord" }
                }
            }
        }
    }
}


data class PlanningGeneratedHandler(
    val plannings: PlanningRepository,
    val availabilities: AvailabilityCalendarRepository,
    val formatter: PlanningMessageFormatter,
    val identityLinks: UserIdentityLinkRepository,
    val channel: MessageChannel,
) : EventHandler<PlanningGenerated> {

    override suspend fun handle(event: PlanningGenerated) {
        val availabilityCalendar = availabilities.get()
        val absents = availabilityCalendar.unavailableFor(event.period)
        val planning = requireNotNull(plannings.findFor(event.period))
        val discordUsers = identityLinks.findUsersByResponsibles(planning.responsibles + absents)
        val msg = formatter.formatCompact(
            planning = planning,
            absents = absents,
            allUsers = discordUsers
        )
        channel.createMessage {
            content = msg
            allowedMentions = mentions(discordUsers.filter { it.responsible !in absents.map { it.name } })
            suppressNotifications = false
        }
    }

    private fun mentions(availables: List<User>): AllowedMentionsBuilder {
        val builder = AllowedMentionsBuilder()
        builder.users.addAll(availables.map { Snowflake(it.id) })
        return builder
    }
}
