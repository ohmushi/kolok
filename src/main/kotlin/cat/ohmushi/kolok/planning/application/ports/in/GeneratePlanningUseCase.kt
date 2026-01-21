package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible

data class GeneratePlanningCommand(
    val period: Period,
    val responsibles: List<Responsible>,
    val responsibilities: List<Responsibility>
)

data class GeneratePlanningResult(
    val planning: Planning,
    val events: List<DomainEvent>,
)

interface GeneratePlanningUseCase {
    fun generatePlanning(command: GeneratePlanningCommand): GeneratePlanningResult
}
