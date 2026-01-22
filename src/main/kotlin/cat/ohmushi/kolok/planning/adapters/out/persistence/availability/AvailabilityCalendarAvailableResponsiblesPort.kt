package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import org.springframework.stereotype.Repository

@Repository
class AvailabilityCalendarAvailableResponsiblesPort(
    private val repository: AvailabilityCalendarRepository
) : AvailableResponsiblesPort {

    override fun getFor(period: Period): List<Responsible> {
        val calendar = requireNotNull(repository.get())
        return calendar.availableFor(period)
    }
}
