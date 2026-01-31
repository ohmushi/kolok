package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

data class ResponsibilityRemovedFrom(
    val from: Period,
    val responsibility: Responsibility
) : DomainEvent