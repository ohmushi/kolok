package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileCatalog
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.availability.AvailabilityCalendar
import org.springframework.stereotype.Repository

@Repository
class FileAvailabilityCalendarRepository(
    private val catalog: FileCatalog,
    private val rosterProvider: RosterProvider,
) : AvailabilityCalendarRepository {

    fun getFor(period: Period): List<Responsible> {
        val cfg = catalog.read()
        val names = catalog.resolveByPeriod(period.start, cfg.responsiblesByPeriod)
        require(names.isNotEmpty()) { "No responsibles configured for period=${period.start}, check='${catalog.path.toAbsolutePath()}'" }
        return names.map { Responsible(it) }
    }

    override fun get(): AvailabilityCalendar? {
        TODO("Not yet implemented")
    }

    override fun save(calendar: AvailabilityCalendar) {
        TODO("Not yet implemented")
    }
}