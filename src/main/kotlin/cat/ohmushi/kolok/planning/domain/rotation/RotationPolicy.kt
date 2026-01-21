package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible

data class RotationRequest(
    val period: Period,
    val responsibles: List<Responsible>,
    val responsibilities: List<Responsibility>,
    val previous: Planning?
)

interface RotationPolicy {
    fun apply(request: RotationRequest): RotationDraft
}

data class RotationDraft(
    val assignments: List<Assignment>
)

class ShiftByOneRotationPolicy: RotationPolicy {
    override fun apply(request: RotationRequest): RotationDraft {
        val previous = requireNotNull(request.previous)
        require(request.responsibles.isNotEmpty())

        val cycle = request.responsibles
        val indexByName = cycle.withIndex().associate { it.value.name to it.index }

        val shifted = previous.assignments.mapNotNull { a ->
            val prevIdx = indexByName[a.responsible.name] ?: return@mapNotNull null
            val next = cycle[(prevIdx + 1) % cycle.size]
            Assignment(next, a.responsibility)
        }

        return RotationDraft(assignments = shifted)
    }
}