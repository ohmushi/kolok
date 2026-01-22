package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.events.DomainEvent

interface EventPublisher {
    fun publish(events: List<DomainEvent>)
}