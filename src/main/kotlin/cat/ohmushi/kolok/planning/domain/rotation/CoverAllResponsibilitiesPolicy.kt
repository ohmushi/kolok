package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.Responsible

class CoverAllResponsibilitiesPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        require(request.responsibles.isNotEmpty())

        val activeResponsibilities = request.responsibilities.toSet()
        val availableResponsibles = request.responsibles.toSet()

        val base = (draft ?: RotationDraft(emptyList()))
            .assignments
            .filter { it.responsibility in activeResponsibilities && it.responsible in availableResponsibles }

        val assigned = base.map { it.responsibility }.toSet()
        val missing = request.responsibilities.filter { it !in assigned }

        if (missing.isEmpty() && base.size == (draft?.assignments?.size ?: base.size)) {
            return draft ?: RotationDraft(base)
        }

        val result = base.toMutableList()

        for (responsibility in missing) {
            val chosen = chooseLeastLoaded(request.responsibles, result)
            result += Assignment(chosen, responsibility)
        }

        return RotationDraft(result)
    }

    private fun chooseLeastLoaded(responsibles: List<Responsible>, current: List<Assignment>): Responsible {
        val loads = current.groupBy { it.responsible }.mapValues { it.value.size }
        return responsibles.minWith(compareBy<Responsible>({ loads.getOrDefault(it, 0) }, { it.name }))
    }
}
