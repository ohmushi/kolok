package cat.ohmushi.kolok.planning.domain.availabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

data class Absence(
    val responsible: Responsible,
    val from: Period,
    val to: Period
) {
    init {
        require(!responsible.name.isBlank())
        require(!to.start.isBefore(from.start))
    }

    fun covers(period: Period): Boolean =
        !period.start.isBefore(from.start) && !period.start.isAfter(to.start)
}

