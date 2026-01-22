package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period

data class PlanningGenerated(val period: Period) : DomainEvent