package cat.ohmushi.kolok.planning.domain.availabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

data class Absence(
    val responsible: Responsible,
    val from: Period,
    val to: Period // TODO replace by periodCount
) {
    init {
        require(!responsible.name.isBlank())
        require(!to.start.isBefore(from.start))
    }

    fun covers(period: Period): Boolean =
        !period.start.isBefore(from.start) && !period.start.isAfter(to.start)

    fun overlaps(other: Absence): Boolean {
        if (this.responsible != other.responsible) return false

        // inclusive: [from, to]
        val aStart = this.from.start
        val aEnd = this.to.start
        val bStart = other.from.start
        val bEnd = other.to.start

        return !(aEnd.isBefore(bStart) || bEnd.isBefore(aStart))
    }
}
