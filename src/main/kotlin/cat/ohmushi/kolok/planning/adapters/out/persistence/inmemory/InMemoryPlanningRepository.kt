package cat.ohmushi.kolok.planning.adapters.out.persistence.inmemory

import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning
import org.springframework.stereotype.Repository

@Repository
class InMemoryPlanningRepository: PlanningRepository {

    private val store = mutableListOf<Planning>()

    override fun findLatestBefore(period: Period): Planning? {
        return store
            .filter { it.period.start.isBefore(period.start) }
            .maxByOrNull { it.period.start }
    }

    override fun save(planning: Planning) {
        store.removeIf { it.period == planning.period }
        store += planning
    }

    fun reset() {
        store.clear()
    }
}