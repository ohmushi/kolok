package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher

@ApplicationService
data class AvailabilityService(
    val availabilityCalendarRepository: AvailabilityCalendarRepository,
    val eventsPublisher: EventsPublisher
) : RecordAbsenceUseCase, CancelAbsenceUseCase {

    override fun recordAbsence(command: RecordAbsenceCommand) {
        val availabilityCalendar = availabilityCalendarRepository.get()

        val updated = availabilityCalendar.recordAbsence(
            responsible = command.responsible,
            from = command.from,
            periodsCount = command.periodsCount
        )

        val (clean, events) = updated.consumeEvents()

        availabilityCalendarRepository.save(clean)
        eventsPublisher.publish(events)
    }

    override fun cancelAbsence(command: CancelAbsenceCommand) {
        val availabilityCalendar = requireNotNull(availabilityCalendarRepository.get()) { "AvailabilityCalendar not initialized" }

        val updated = availabilityCalendar.cancelAbsence(
            responsible = command.responsible,
            from = command.from,
            periodsCount = command.periodsCount
        )

        val (clean, events) = updated.consumeEvents()

        availabilityCalendarRepository.save(clean)
        eventsPublisher.publish(events)
    }
}
