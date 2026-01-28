package cat.ohmushi.kolok.planning.adapters.`in`.discord

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordConnexion
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

sealed interface DiscordEventHandler<E: Event> {
    suspend fun handle(event: E)
}

@Component
class DiscordInboundAdapter(
    private val discordConnexion: DiscordConnexion,
    private val messageHandler: MessageCreatedHandler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @EventListener(ApplicationReadyEvent::class)
    fun registerListeners() {
        scope.launch {
            discordConnexion.withKord { kord ->
                kord.on<MessageCreateEvent> { messageHandler.handle(this) }
            }
        }
    }
}

@Component
class MessageCreatedHandler : DiscordEventHandler<MessageCreateEvent> {
    override suspend fun handle(event: MessageCreateEvent) {
        if (event.message.author?.isBot != false) return

        // all clear, give them the pong!
        event.message.channel.createMessage("pong!")
    }
}