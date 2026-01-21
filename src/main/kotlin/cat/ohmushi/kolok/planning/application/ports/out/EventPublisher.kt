package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.DomainEvent

interface EventPublisher {
    fun publish(events: List<DomainEvent>)
}