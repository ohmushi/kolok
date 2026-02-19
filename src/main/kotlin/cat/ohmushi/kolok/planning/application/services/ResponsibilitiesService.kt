package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ActiveResponsibilitiesQuery
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryActiveResponsibilitiesUseCase
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

@ApplicationService
data class ResponsibilitiesService(
    private val repository: ResponsibilitiesCatalogRepository,
) : QueryActiveResponsibilitiesUseCase {

    override fun activeResponsibilitiesFor(command: ActiveResponsibilitiesQuery): List<Responsibility> {
        val catalog = requireNotNull(repository.get())
        return catalog.activeFor(command.period)
    }
}

