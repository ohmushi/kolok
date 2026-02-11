package cat.ohmushi.kolok.planning.domain.availabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

data class Absence(
    val responsible: Responsible,
    val from: Period,
    val periodsCount: Int,
) {
    init {
        require(!responsible.name.isBlank())
        require(periodsCount >= 1) { "periodsCount must be >= 1" }
    }

    fun covers(period: Period): Boolean {
        val end = endInclusive()
        return !period.start.isBefore(from.start) && !period.start.isAfter(end.start)
    }

    fun overlaps(other: Absence): Boolean {
        if (this.responsible != other.responsible) return false

        val aStart = this.from.start
        val aEnd = this.endInclusive().start
        val bStart = other.from.start
        val bEnd = other.endInclusive().start

        return !(aEnd.isBefore(bStart) || bEnd.isBefore(aStart))
    }

    fun endInclusive(): Period =
        from.plus((periodsCount - 1).toLong())
}
