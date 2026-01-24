package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.availability.AvailabilityCalendar
import org.springframework.stereotype.Repository

@Repository
class AvailableResponsiblesAdapter(
    private val repository: AvailabilityCalendarRepository,
    private val rosterProvider: RosterProvider,
) : AvailableResponsiblesPort {
    override fun getFor(period: Period): List<Responsible> {
        val calendar = repository.get() ?: AvailabilityCalendar.create(rosterProvider.roster())
        return calendar.availableFor(period)
    }
}
