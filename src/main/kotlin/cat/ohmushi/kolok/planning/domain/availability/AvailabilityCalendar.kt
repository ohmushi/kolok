package cat.ohmushi.kolok.planning.domain.availability

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.events.AbsenceCancelled
import cat.ohmushi.kolok.planning.domain.events.AbsenceRecorded
import cat.ohmushi.kolok.planning.domain.events.DomainEvent

class AvailabilityCalendar private constructor(
    val roster: Set<Responsible>,
    private val absences: List<Absence>,
    private val pendingEvents: List<DomainEvent>
) {

    companion object {
        fun create(roster: Set<Responsible>): AvailabilityCalendar {
            require(roster.isNotEmpty())
            require(roster.all { it.name.isNotBlank() })
            return AvailabilityCalendar(roster = roster, absences = emptyList(), pendingEvents = emptyList())
        }
    }

    fun availableFor(period: Period): List<Responsible> {
        val absent = unavailableFor(period).toSet()

        return roster.filterNot { it in absent }
    }

    fun recordAbsence(responsible: Responsible, from: Period, periodsCount: Int = 1): AvailabilityCalendar {
        require(responsible in roster)
        require(periodsCount >= 1) { "periodsCount must be >= 1" }

        val toIncluded = from.plus((periodsCount - 1).toLong())
        val absence = Absence(responsible, from, toIncluded)
        if (absences.any { it == absence }) return this

        val nextAbsences = absences + absence
        val event = AbsenceRecorded(responsible = responsible, from = from, periodsCount = periodsCount)

        return AvailabilityCalendar(
            roster = roster,
            absences = nextAbsences,
            pendingEvents = pendingEvents + event
        )
    }

    fun cancelAbsence(responsible: Responsible, from: Period, periodsCount: Int = 1): AvailabilityCalendar {
        require(responsible in roster)
        require(periodsCount >= 1) { "periodsCount must be >= 1" }

        val toIncluded = from.plus((periodsCount - 1).toLong())
        val target = Absence(responsible, from, toIncluded)
        require(absences.any { it == target })

        val nextAbsences = absences.filterNot { it == target }
        val event = AbsenceCancelled(responsible = responsible, from = from, periodsCount = periodsCount)

        return AvailabilityCalendar(
            roster = roster,
            absences = nextAbsences,
            pendingEvents = pendingEvents + event
        )
    }

    fun consumeEvents(): Pair<AvailabilityCalendar, List<DomainEvent>> =
        AvailabilityCalendar(roster = roster, absences = absences, pendingEvents = emptyList()) to pendingEvents

    fun snapshotAbsences(): List<Absence> =
        absences.toList()

    fun unavailableFor(period: Period): List<Responsible> {
        return absences
            .filter { it.covers(period) }
            .map { it.responsible }
            .sortedBy { it.name }
    }
}