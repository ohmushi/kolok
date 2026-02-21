package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface QueryResponsibilitiesForPeriodUseCase {
    fun responsibilitiesFor(command: ResponsibilitiesForPeriodQuery): List<Responsibility>
}

data class ResponsibilitiesForPeriodQuery(
    val period: Period,
)

