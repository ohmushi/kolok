package cat.ohmushi.kolok.planning.adapters.out.persistence.responsibilities

import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import org.springframework.stereotype.Repository

@Repository
class ActiveResponsibilitiesAdapter(
    private val repository: ResponsibilitiesCatalogRepository
) : ActiveResponsibilitiesPort {

    override fun getFor(period: Period): List<Responsibility> {
        val responsibilities = requireNotNull(repository.get())
        return responsibilities.activeFor(period)
    }
}
