package cat.ohmushi.kolok.planning.domain.planning

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Period(val start: LocalDate) {
    init {
        require(start.dayOfWeek == DayOfWeek.MONDAY, { "Period must start on a Monday" })
    }

    fun next(): Period {
        return plus(1)
    }

    fun plus(amount: Long): Period =  Period(start.plusWeeks(amount))

    operator fun rangeTo(other: Period): List<Period> {
        require(!other.start.isBefore(this.start)) { "Cannot build a range to a past period" }

        val out = mutableListOf<Period>()
        var cursor = this
        while (!cursor.start.isAfter(other.start)) {
            out += cursor
            cursor = cursor.next()
        }
        return out
    }

    companion object {
        fun firstAfter(date: LocalDate) = Period(date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
    }
}