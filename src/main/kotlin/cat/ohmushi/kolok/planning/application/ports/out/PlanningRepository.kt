package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning

interface PlanningRepository {
    fun findLatestBefore(period: Period): Planning?
    fun save(planning: Planning)
}
