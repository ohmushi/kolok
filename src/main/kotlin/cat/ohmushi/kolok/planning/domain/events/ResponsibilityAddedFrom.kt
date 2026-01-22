package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility

data class ResponsibilityAddedFrom(
    val from: Period,
    val responsibility: Responsibility
) : DomainEvent