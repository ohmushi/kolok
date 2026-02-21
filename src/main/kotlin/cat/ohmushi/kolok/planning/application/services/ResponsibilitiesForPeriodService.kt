package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.AddResponsibilityCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.AddResponsibilityUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryResponsibilitiesForPeriodUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

@ApplicationService
data class ResponsibilitiesService(
    private val repository: ResponsibilitiesCatalogRepository,
) : QueryResponsibilitiesForPeriodUseCase, AddResponsibilityUseCase {

    override fun responsibilitiesFor(command: ResponsibilitiesForPeriodQuery): List<Responsibility> {
        val catalog = requireNotNull(repository.get())
        return catalog.activeFor(command.period)
    }

    override fun addResponsibility(command: AddResponsibilityCommand) {
        val catalog = requireNotNull(repository.get())
        val updated = catalog.addFrom(from = command.from, responsibility = command.responsibility)
        if (updated !== catalog) {
            repository.save(updated)
        }
    }
}
