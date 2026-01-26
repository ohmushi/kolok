package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.events.DomainEvent

interface EventsPublisher {
    fun publish(events: List<DomainEvent>)
}