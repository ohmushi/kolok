package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

data class AbsenceCancelled(
    val responsible: Responsible,
    val from: Period,
) : DomainEvent
