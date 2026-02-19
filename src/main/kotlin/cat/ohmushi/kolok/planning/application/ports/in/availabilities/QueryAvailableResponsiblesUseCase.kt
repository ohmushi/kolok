package cat.ohmushi.kolok.planning.application.ports.`in`.availabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

interface QueryAvailableResponsiblesUseCase {
    fun availableResponsiblesFor(query: AvailableResponsiblesQuery): List<Responsible>
}

data class AvailableResponsiblesQuery(
    val period: Period,
)

