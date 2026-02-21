package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryResponsibilitiesForPeriodUseCase
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

@ApplicationService
data class ResponsibilitiesForPeriodService(
    private val repository: ResponsibilitiesCatalogRepository,
) : QueryResponsibilitiesForPeriodUseCase {

    override fun ResponsibilitiesFor(command: ResponsibilitiesForPeriodQuery): List<Responsibility> {
        val catalog = requireNotNull(repository.get())
        return catalog.activeFor(command.period)
    }
}

