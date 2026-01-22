package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible

data class AbsenceCancelled(
    val responsible: Responsible,
    val from: Period,
    val to: Period
) : DomainEvent