package cat.ohmushi.kolok.planning.domain

import java.time.DayOfWeek
import java.time.LocalDate

data class Period(val start: LocalDate) {
    init {
        require(start.dayOfWeek == DayOfWeek.MONDAY)
    }
    fun next(): Period {
        return Period(start.plusWeeks(1))
    }
}