package cat.ohmushi.kolok.planning.domain

import cat.ohmushi.kolok.planning.domain.rotation.RotationDraft
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest

interface PlanningFactory {
    fun from(request: RotationRequest, draft: RotationDraft): Planning
}

class DefaultPlanningFactory: PlanningFactory {
    override fun from(
        request: RotationRequest,
        draft: RotationDraft
    ): Planning {
        TODO("Not yet implemented")
    }

}