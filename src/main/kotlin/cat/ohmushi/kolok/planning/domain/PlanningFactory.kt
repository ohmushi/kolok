package cat.ohmushi.kolok.planning.domain

import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import cat.ohmushi.kolok.planning.domain.rotation.RotationDraft
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest

interface PlanningFactory {
    fun from(request: RotationRequest, draft: RotationDraft): Planning
}

@ApplicationComponent
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