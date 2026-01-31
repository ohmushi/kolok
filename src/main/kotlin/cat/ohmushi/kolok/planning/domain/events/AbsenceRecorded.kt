package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

data class AbsenceRecorded(
    val responsible: Responsible,
    val from: Period,
    val periodsCount: Int,
) : DomainEvent {
    init {
        require(periodsCount >= 1) { "periodsCount must be >= 1" }
    }
}
