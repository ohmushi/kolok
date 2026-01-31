package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.springframework.stereotype.Repository

@Repository
class AvailableResponsiblesAdapter(
    private val repository: AvailabilityCalendarRepository
) : AvailableResponsiblesPort {
    override fun getFor(period: Period): List<Responsible> {
        val calendar = repository.get()
        return calendar.availableFor(period)
    }
}
