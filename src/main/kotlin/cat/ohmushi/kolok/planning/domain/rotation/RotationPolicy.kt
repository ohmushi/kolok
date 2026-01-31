package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible

data class RotationRequest(
    val period: Period,
    val responsibles: List<Responsible>,
    val responsibilities: List<Responsibility>,
    val previous: Planning?
)

data class RotationDraft(
    val assignments: List<Assignment>
)

interface RotationPolicy {
    fun apply(request: RotationRequest, draft: RotationDraft? = null): RotationDraft
}

