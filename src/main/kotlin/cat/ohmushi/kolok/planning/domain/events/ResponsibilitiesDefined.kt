package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

data class ResponsibilitiesDefined(
    val from: Period,
    val responsibilities: Set<Responsibility>
) : DomainEvent