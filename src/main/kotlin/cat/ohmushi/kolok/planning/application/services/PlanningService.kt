package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningResult
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.PlanningFactory
import cat.ohmushi.kolok.planning.domain.PlanningGenerated
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest

data class PlanningService (
    val planningRepository: PlanningRepository,
    val rotationPolicy: RotationPolicy,
    val planningFactory: PlanningFactory,
    val eventPublisher: EventPublisher,
)
    : GeneratePlanningUseCase
{
    override fun generatePlanning(command: GeneratePlanningCommand): GeneratePlanningResult {
        val previous = planningRepository.findLatestBefore(command.period)

        val request = RotationRequest(
            period = command.period,
            responsibles = command.responsibles,
            responsibilities = command.responsibilities,
            previous = previous
        )

        val draft = rotationPolicy.apply(request)
        val planning = planningFactory.from(request, draft)

        planningRepository.save(planning)

        val events = listOf(PlanningGenerated(period = planning.period))

        eventPublisher.publish(events)
        return GeneratePlanningResult(planning = planning, events = events)
    }

}
