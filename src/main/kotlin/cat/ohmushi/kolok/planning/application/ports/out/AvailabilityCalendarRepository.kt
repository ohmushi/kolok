package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.availability.AvailabilityCalendar

interface AvailabilityCalendarRepository {
    fun get(): AvailabilityCalendar?
    fun save(calendar: AvailabilityCalendar)
}