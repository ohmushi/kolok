package cat.ohmushi.kolok.planning.domain.planning

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import kotlin.collections.plus

class Planning(
    val period: Period,
    val responsibles: List<Responsible>,
    val responsibilities: List<Responsibility>,
    val assignments: List<Assignment>,
) {
    init {
        require(responsibles.isNotEmpty())
        require(responsibilities.isNotEmpty())

        assignments.forEach {
            require(responsibles.contains(it.responsible))
            require(responsibilities.contains(it.responsibility))
        }

        val assignedResponsibilities = assignments.map { it.responsibility }
        require(assignedResponsibilities.toSet().size == assignedResponsibilities.size)
        require(assignedResponsibilities.toSet() == responsibilities.toSet())

        val loadByResponsible = assignments.groupBy { it.responsible }.mapValues { it.value.size }

        if (responsibilities.size >= responsibles.size) {
            responsibles.forEach {
                require(loadByResponsible.getOrDefault(it, 0) > 0)
            }
        }

        val loads = responsibles.map { loadByResponsible.getOrDefault(it, 0) }
        if (loads.isNotEmpty()) {
            require(loads.maxOrNull()!! - loads.minOrNull()!! <= 1)
        }
    }

    fun assignmentsFor(responsible: Responsible): List<Assignment> {
        return assignments.filter { it.responsible == responsible }
    }

    fun loadPerResponsible(): Map<Responsible, Int> {
        val counts = assignments.groupBy { it.responsible }.mapValues { it.value.size }
        return responsibles.associateWith { counts.getOrDefault(it, 0) }
    }

    fun addResponsibility(newResponsibility: Responsibility): Planning {
        require(!responsibilities.contains(newResponsibility))

        val newResponsibilities = responsibilities + newResponsibility
        val currentLoads = loadPerResponsible()

        //  TODO améliorable, et surtout à load égal ce sera tjrs le même selectionné, introduire la notion de loadPoint ?
        val chosen = responsibles
            .sortedWith(compareBy({ currentLoads.getOrDefault(it, 0) }, { it.name }))
            .first()

        val newAssignments = assignments + Assignment(chosen, newResponsibility)

        return Planning(
            period = period,
            responsibles = responsibles,
            responsibilities = newResponsibilities,
            assignments = newAssignments
        )
    }

    fun removeResponsibility(responsibility: Responsibility): Planning {
        require(responsibilities.contains(responsibility))

        val newResponsibilities = responsibilities.filterNot { it == responsibility }
        val newAssignments = assignments.filterNot { it.responsibility == responsibility }

        require(newResponsibilities.isNotEmpty())

        return Planning(
            period = period,
            responsibles = responsibles,
            responsibilities = newResponsibilities,
            assignments = newAssignments
        )
    }
}