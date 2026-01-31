package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.availabilities.AvailabilityCalendar

interface AvailabilityCalendarRepository {
    fun get(): AvailabilityCalendar
    fun save(calendar: AvailabilityCalendar)
}