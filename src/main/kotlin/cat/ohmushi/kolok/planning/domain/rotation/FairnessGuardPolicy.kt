package cat.ohmushi.kolok.planning.domain.rotation

class FairnessGuardPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        requireNotNull(draft)
        require(request.responsibles.isNotEmpty())
        require(request.responsibilities.isNotEmpty())

        val activeResponsibles = request.responsibles.toSet()
        val activeResponsibilities = request.responsibilities.toSet()

        draft.assignments.forEach {
            require(it.responsible in activeResponsibles)
            require(it.responsibility in activeResponsibilities)
        }

        val assignedResponsibilities = draft.assignments.map { it.responsibility }
        require(assignedResponsibilities.size == assignedResponsibilities.toSet().size)
        require(assignedResponsibilities.toSet() == activeResponsibilities)

        val loadsByResponsible = draft.assignments.groupBy { it.responsible }.mapValues { it.value.size }
        val loads = request.responsibles.map { loadsByResponsible.getOrDefault(it, 0) }

        val max = loads.maxOrNull() ?: 0
        val min = loads.minOrNull() ?: 0
        require(max - min <= 1)

        if (request.responsibilities.size >= request.responsibles.size) {
            request.responsibles.forEach { require(loadsByResponsible.getOrDefault(it, 0) > 0) }
        }

        return draft
    }
}
