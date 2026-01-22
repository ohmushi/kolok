package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning

data class GeneratePlanningCommand(
    val period: Period,
)

data class GeneratePlanningResult(
    val planning: Planning,
    val events: List<DomainEvent>,
)

interface GeneratePlanningUseCase {
    fun generatePlanning(command: GeneratePlanningCommand): GeneratePlanningResult
}
