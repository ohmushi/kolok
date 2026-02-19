package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface QueryActiveResponsibilitiesUseCase {
    fun activeResponsibilitiesFor(command: ActiveResponsibilitiesQuery): List<Responsibility>
}

data class ActiveResponsibilitiesQuery(
    val period: Period,
)

