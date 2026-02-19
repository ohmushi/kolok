package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.AvailableResponsiblesQuery
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.QueryAvailableResponsiblesUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningResult
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ActiveResponsibilitiesQuery
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.QueryActiveResponsibilitiesUseCase
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.planning.PlanningFactory
import cat.ohmushi.kolok.planning.domain.events.PlanningGenerated
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest

@ApplicationService
data class PlanningService (
    val planningRepository: PlanningRepository,
    val rotationPolicy: RotationPolicy,
    val planningFactory: PlanningFactory,
    val eventsPublisher: EventsPublisher,
    val queryAvailableResponsiblesUseCase: QueryAvailableResponsiblesUseCase,
    val queryActiveResponsibilitiesUseCase: QueryActiveResponsibilitiesUseCase,
)
    : GeneratePlanningUseCase
{
    override fun generatePlanning(command: GeneratePlanningCommand): GeneratePlanningResult {
        val previous = planningRepository.findLatestBefore(command.period)
        val events = mutableListOf<DomainEvent>()

        val request = RotationRequest(
            period = command.period,
            responsibles = queryAvailableResponsiblesUseCase.availableResponsiblesFor(
                AvailableResponsiblesQuery(period = command.period)
            ),
            responsibilities = queryActiveResponsibilitiesUseCase.activeResponsibilitiesFor(
                ActiveResponsibilitiesQuery(period = command.period)
            ),
            previous = previous
        )

        val draft = rotationPolicy.apply(request)
        val planning = planningFactory.from(request, draft)

        planningRepository.save(planning)

        events.add(PlanningGenerated(period = command.period))

        eventsPublisher.publish(events)
        return GeneratePlanningResult(planning = planning, events = events)
    }

}
