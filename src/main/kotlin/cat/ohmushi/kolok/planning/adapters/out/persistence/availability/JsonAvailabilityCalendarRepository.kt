package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.adapters.out.persistence.AbsenceFileEntry
import cat.ohmushi.kolok.planning.adapters.out.persistence.JsonPersistence
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.availability.AvailabilityCalendar
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JsonAvailabilityCalendarRepository(
    private val jsonPersistence: JsonPersistence,
    private val rosterProvider: RosterProvider
) : AvailabilityCalendarRepository {

    override fun get(): AvailabilityCalendar? {
        val file = jsonPersistence.read()
        val roster = rosterProvider.roster()

        val base = AvailabilityCalendar.create(roster)

        val rebuilt = file.absences.fold(base) { acc, a ->
            acc.recordAbsence(
                responsible = Responsible(a.responsible),
                from = Period(LocalDate.parse(a.from)),
                to = Period(LocalDate.parse(a.to))
            )
        }

        val (clean, _) = rebuilt.consumeEvents()
        return clean
    }

    override fun save(calendar: AvailabilityCalendar) {
        val file = jsonPersistence.read()

        val absences = calendar.snapshotAbsences().map {
            AbsenceFileEntry(
                responsible = it.responsible.name,
                from = it.from.start.toString(),
                to = it.to.start.toString()
            )
        }

        val updated = file.copy(absences = absences)
        jsonPersistence.write(updated)
    }
}