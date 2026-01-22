package cat.ohmushi.kolok.planning.domain.rotation

class ProjectPreviousAssignmentsPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        if (draft != null) return draft

        val previous = request.previous ?: return RotationDraft(assignments = emptyList())

        val activeResponsibles = request.responsibles.toSet()
        val activeResponsibilities = request.responsibilities.toSet()

        val projected = previous.assignments.filter { a ->
            a.responsible in activeResponsibles && a.responsibility in activeResponsibilities
        }

        return RotationDraft(assignments = projected)
    }
}