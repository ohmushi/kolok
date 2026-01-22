package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.planning.Planning
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible

class BalanceLoadPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        requireNotNull(draft)
        require(request.responsibles.isNotEmpty())

        val activeResponsibles = request.responsibles
        val activeResponsibilities = request.responsibilities.toSet()

        val filtered = draft.assignments.filter { it.responsible in activeResponsibles && it.responsibility in activeResponsibilities }
        val current = filtered.toMutableList()

        val targets = targetLoads(request.previous, activeResponsibles, request.responsibilities)

        if (loads(current, activeResponsibles) == targets) return draft

        while (true) {
            val loadMap = loads(current, activeResponsibles)
            val donor = activeResponsibles
                .filter { loadMap.getValue(it) > targets.getValue(it) }
                .maxWithOrNull(compareBy<Responsible>({ loadMap.getValue(it) }, { it.name }))
                ?: break

            val receiver = activeResponsibles
                .filter { loadMap.getValue(it) < targets.getValue(it) }
                .minWithOrNull(compareBy<Responsible>({ loadMap.getValue(it) }, { it.name }))
                ?: break

            val toMove = current
                .filter { it.responsible == donor }.minByOrNull { it.responsibility.name }
                ?: break

            val idx = current.indexOf(toMove)
            current[idx] = Assignment(receiver, toMove.responsibility)
        }

        return RotationDraft(current)
    }

    private fun targetLoads(previous: Planning?, responsibles: List<Responsible>, responsibilities: List<Responsibility>): Map<Responsible, Int> {
        val n = responsibles.size
        val m = responsibilities.size
        val base = m / n
        val extraCount = m % n

        val order = if (previous == null) {
            responsibles.sortedBy { it.name }
        } else {
            val prevLoads = previous.assignments
                .filter { it.responsible in responsibles && it.responsibility in responsibilities.toSet() }
                .groupBy { it.responsible }
                .mapValues { it.value.size }
            responsibles.sortedWith(compareBy({ prevLoads.getOrDefault(it, 0) }, { it.name }))
        }

        val extras = order.take(extraCount).toSet()
        return responsibles.associateWith { if (it in extras) base + 1 else base }
    }

    private fun loads(assignments: List<Assignment>, responsibles: List<Responsible>): Map<Responsible, Int> {
        val by = assignments.groupBy { it.responsible }.mapValues { it.value.size }
        return responsibles.associateWith { by.getOrDefault(it, 0) }
    }
}
