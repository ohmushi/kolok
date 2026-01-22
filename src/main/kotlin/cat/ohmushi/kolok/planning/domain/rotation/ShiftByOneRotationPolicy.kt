package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment

class ShiftByOneRotationPolicy: RotationPolicy {
    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        val previous = requireNotNull(draft)
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