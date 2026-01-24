package cat.ohmushi.kolok.planning.domain.planning

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.domain.rotation.RotationDraft
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest

interface PlanningFactory {
    fun from(request: RotationRequest, draft: RotationDraft): Planning
}

@ApplicationService
class DefaultPlanningFactory: PlanningFactory {
    override fun from(
        request: RotationRequest,
        draft: RotationDraft
    ): Planning {
        return Planning(
            period = request.period,
            responsibles = request.responsibles,
            responsibilities = request.responsibilities,
            assignments = draft.assignments
        )
    }

}