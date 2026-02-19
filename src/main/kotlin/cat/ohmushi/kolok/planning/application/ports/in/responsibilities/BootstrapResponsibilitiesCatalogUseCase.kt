package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface BootstrapResponsibilitiesCatalogUseCase {
    fun bootstrapResponsibilitiesCatalog(command: BootstrapResponsibilitiesCatalogCommand)
}

data class BootstrapResponsibilitiesCatalogCommand(
    val initialFrom: Period,
    val responsibilities: Set<Responsibility>,
)

