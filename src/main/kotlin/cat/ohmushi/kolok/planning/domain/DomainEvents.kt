package cat.ohmushi.kolok.planning.domain

sealed interface DomainEvent

data class PlanningGenerated(val period: Period) : DomainEvent
data class ResponsibilitiesChanged(val period: Period) : DomainEvent
