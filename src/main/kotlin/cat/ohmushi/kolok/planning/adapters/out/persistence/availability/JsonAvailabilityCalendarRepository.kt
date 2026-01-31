package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.adapters.infrastructure.AbsenceFileEntry
import cat.ohmushi.kolok.planning.adapters.infrastructure.JsonPersistence
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.availabilities.AvailabilityCalendar
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JsonAvailabilityCalendarRepository(
    private val jsonPersistence: JsonPersistence,
    private val rosterProvider: RosterProvider
) : AvailabilityCalendarRepository {

    override fun get(): AvailabilityCalendar {
        val file = jsonPersistence.read()
        val roster = rosterProvider.roster()

        val base = AvailabilityCalendar.create(roster)

        val rebuilt = file.absences.fold(base) { acc, a ->
            val from = Period(LocalDate.parse(a.from))
            val periodsCount = a.periodsCount.takeIf { it >= 1 } ?: 1

            acc.recordAbsence(
                responsible = Responsible(a.responsible),
                from = from,
                periodsCount = periodsCount
            )
        }

        val (clean, _) = rebuilt.consumeEvents()
        return clean
    }

    override fun save(calendar: AvailabilityCalendar) {
        val file = jsonPersistence.read()

        val absences = calendar.snapshotAbsences().map { absence ->
            AbsenceFileEntry(
                responsible = absence.responsible.name,
                from = absence.from.start.toString(),
                periodsCount = (absence.from..absence.to).size,
            )
        }

        val updated = file.copy(absences = absences)
        jsonPersistence.write(updated)
    }
}