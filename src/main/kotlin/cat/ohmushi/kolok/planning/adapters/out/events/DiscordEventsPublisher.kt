package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordConnexion
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.DiscordIdentityLinkRepository
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.events.PlanningGenerated
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DiscordEventsPublisher(
    private val discordConnexion: DiscordConnexion,
    @Value("\${discord.planning.channel}") private val channelId: String,
    val plannings: PlanningRepository,
    val availabilities: AvailabilityCalendarRepository,
    val formatter: PlanningMessageFormatter,
    val identityLinks: DiscordIdentityLinkRepository,
) : EventsPublisher {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun publish(events: List<DomainEvent>) {
        scope.launch {
            discordConnexion.withKord { kord ->
                val channel = requireNotNull(
                    kord.getChannelOf<MessageChannel>(Snowflake(channelId)),
                    { "Channel not found" })
                for (event in events) {
                    when (event) {
                        is PlanningGenerated -> {
                            val availabilityCalendar = availabilities.get()
                            val absents = availabilityCalendar.unavailableFor(event.period)
                            val planning = requireNotNull(plannings.findFor(event.period))
                            val discordUsers =
                                requireNotNull(identityLinks.findDiscordSnowflakesByResponsibles(planning.responsibles + absents))
                            val msg = formatter.formatCompact(
                                planning = planning,
                                absents = absents,
                                discordUsers = discordUsers
                            )
                            channel.createMessage(msg)
                        }

                        else -> {
                            // Do nothing
                        }
                    }
                }
            }

        }
    }
}
