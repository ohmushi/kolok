package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningResult
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.planning.PlanningFactory
import cat.ohmushi.kolok.planning.domain.events.PlanningGenerated
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest
import org.springframework.stereotype.Service

@Service
data class PlanningService (
    val planningRepository: PlanningRepository,
    val rotationPolicy: RotationPolicy,
    val planningFactory: PlanningFactory,
    val eventPublisher: EventPublisher,
    val availableResponsiblesPort: AvailableResponsiblesPort,
    val activeResponsibilitiesPort: ActiveResponsibilitiesPort,
)
    : GeneratePlanningUseCase
{
    override fun generatePlanning(command: GeneratePlanningCommand): GeneratePlanningResult {
        val previous = planningRepository.findLatestBefore(command.period)

        val request = RotationRequest(
            period = command.period,
            responsibles = availableResponsiblesPort.getFor(command.period),
            responsibilities = activeResponsibilitiesPort.getFor(command.period),
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
