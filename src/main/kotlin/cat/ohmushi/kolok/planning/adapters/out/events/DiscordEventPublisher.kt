package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordConnexion
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
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
class DiscordEventPublisher(
    private val discordConnexion: DiscordConnexion,
    @Value("\${discord.planning.channel}") private val channel: String,
): EventPublisher {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun publish(events: List<DomainEvent>) {
        scope.launch {
            discordConnexion.withKord { kord ->
                for (event in events) {
                    when (event) {
                        is PlanningGenerated -> {
                            val channel = kord.getChannelOf<MessageChannel>(Snowflake(channel))
                            println("channel: $channel")
                            channel?.createMessage("<@288729200175611905> test")
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