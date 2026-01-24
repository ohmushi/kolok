package cat.ohmushi.kolok.planning.domain.planning

import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import cat.ohmushi.kolok.planning.domain.rotation.RotationDraft
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest
import org.springframework.stereotype.Service

interface PlanningFactory {
    fun from(request: RotationRequest, draft: RotationDraft): Planning
}

@Service
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