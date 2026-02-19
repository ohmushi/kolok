package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.AvailableResponsiblesQuery
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.QueryAvailableResponsiblesUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.availabilities.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

@ApplicationService
data class AvailabilitiesService(
    val availabilityCalendarRepository: AvailabilityCalendarRepository,
    val eventsPublisher: EventsPublisher
) : RecordAbsenceUseCase, CancelAbsenceUseCase, QueryAvailableResponsiblesUseCase {

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
        )

        val (clean, events) = updated.consumeEvents()

        availabilityCalendarRepository.save(clean)
        eventsPublisher.publish(events)
    }

    override fun availableResponsiblesFor(query: AvailableResponsiblesQuery): List<Responsible> {
        val calendar = availabilityCalendarRepository.get()
        return calendar.availableFor(query.period)
    }
}
