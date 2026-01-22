package cat.ohmushi.kolok.planning.domain.availability

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible

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

