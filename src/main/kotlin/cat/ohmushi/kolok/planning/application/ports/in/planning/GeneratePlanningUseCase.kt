package cat.ohmushi.kolok.planning.application.ports.`in`.planning

import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning

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
