package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible

data class AbsenceRecorded(
    val responsible: Responsible,
    val from: Period,
    val to: Period
) : DomainEvent