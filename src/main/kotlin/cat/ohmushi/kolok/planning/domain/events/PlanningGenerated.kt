package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.planning.Planning

data class PlanningGenerated(val planning: Planning) : DomainEvent