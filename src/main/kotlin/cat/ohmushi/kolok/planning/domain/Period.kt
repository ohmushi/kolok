package cat.ohmushi.kolok.planning.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Period(val start: LocalDate) {
    init {
        require(start.dayOfWeek == DayOfWeek.MONDAY, { "Period must start on a Monday" })
    }
    fun next(): Period {
        return Period(start.plusWeeks(1))
    }

    companion object {
        fun firstAfter(date: LocalDate) = Period(date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
    }
}