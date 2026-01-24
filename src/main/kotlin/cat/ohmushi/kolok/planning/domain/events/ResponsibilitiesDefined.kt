package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility

data class ResponsibilitiesDefined(
    val from: Period,
    val responsibilities: Set<Responsibility>
) : DomainEvent