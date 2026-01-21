package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.domain.DomainEvent

class DiscordEventPublisher: EventPublisher {
    override fun publish(events: List<DomainEvent>) {
        TODO("Not yet implemented")
    }
}