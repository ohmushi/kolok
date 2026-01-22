package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment

class BootstrapIfNoPreviousPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        if (draft != null && draft.assignments.isNotEmpty()) return draft
        if (request.previous != null) return draft ?: RotationDraft(emptyList())

        val responsibles = request.responsibles
        val responsibilities = request.responsibilities

        val assignments = responsibilities.mapIndexed { index, responsibility ->
            val responsible = responsibles[index % responsibles.size]
            Assignment(responsible, responsibility)
        }

        return RotationDraft(assignments)
    }
}
