package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment

class ShiftByOneRotationPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        require(request.responsibles.isNotEmpty())

        val source = when {
            draft != null -> draft.assignments
            request.previous != null -> request.previous.assignments
            else -> emptyList()
        }

        if (source.isEmpty()) return draft ?: RotationDraft(emptyList())

        val cycle = request.responsibles
        val indexByResponsible = cycle.withIndex().associate { it.value to it.index }

        val shifted = source.mapNotNull { a ->
            val prevIdx = indexByResponsible[a.responsible] ?: return@mapNotNull null
            val next = cycle[(prevIdx + 1) % cycle.size]
            Assignment(next, a.responsibility)
        }

        return RotationDraft(shifted)
    }
}
