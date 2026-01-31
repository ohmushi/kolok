package cat.ohmushi.kolok.planning.domain.responsibilities

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

data class ResponsibilitiesVersion(
    val from: Period,
    val responsibilities: Set<Responsibility>
) {
    init {
        require(responsibilities.isNotEmpty())
        require(responsibilities.all { it.name.isNotBlank() })
        require(responsibilities.size == responsibilities.distinct().size)
    }
}

