package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.domain.availabilities.AvailabilityCalendar
import cat.ohmushi.kolok.planning.domain.events.AbsenceCancelled
import cat.ohmushi.kolok.planning.domain.events.AbsenceRecorded
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AvailabilitiesServiceTest {

    private val p1 = Period(LocalDate.of(2026, 1, 12))
    private val p2 = Period(LocalDate.of(2026, 1, 19))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    open class Base(roster: Set<Responsible>) {
        protected val repo = InMemoryAvailabilityCalendarRepository(roster)
        protected val publisher = CapturingEventsPublisher()
        protected val service = AvailabilitiesService(repo, publisher)

        protected fun recordUseCase(): RecordAbsenceUseCase = service
        protected fun cancelUseCase(): CancelAbsenceUseCase = service
    }

    @Nested
    inner class RecordAbsenceUseCaseTest : Base(setOf(charles, fabio, theo)) {

        @BeforeEach
        fun setUp() {
            repo.reset()
            publisher.publishedEvents.clear()
        }

        @Test
        fun execute_shouldRecordAbsence_save_andPublishEvent() {
            recordUseCase().recordAbsence(
                RecordAbsenceCommand(
                    responsible = theo,
                    from = p1,
                )
            )

            assertThat(repo.saved).isNotNull
            assertThat(repo.saved!!.availableFor(p1)).containsExactly(charles, fabio)
            assertThat(repo.saved!!.availableFor(p2)).containsExactly(charles, fabio, theo)

            assertThat(publisher.publishedEvents).containsExactly(
                AbsenceRecorded(responsible = theo, from = p1, periodsCount = 1)
            )
        }
    }

    @Nested
    inner class CancelAbsenceUseCaseTest : Base(setOf(fabio, theo, charles)) {

        @BeforeEach
        fun setUp() {
            repo.reset()
            publisher.publishedEvents.clear()
        }

        @Test
        fun execute_shouldCancelAbsence_save_andPublishEvent() {
            recordUseCase().recordAbsence(RecordAbsenceCommand(theo, p1, periodsCount = 2))
            val before = publisher.publishedEvents.toList()

            cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1))

            assertThat(repo.saved).isNotNull
            assertThat(repo.saved!!.availableFor(p1)).containsExactlyInAnyOrder(charles, fabio, theo)
            assertThat(repo.saved!!.availableFor(p2)).containsExactlyInAnyOrder(charles, fabio, theo)

            assertThat(publisher.publishedEvents).containsExactlyElementsOf(before + listOf(
                AbsenceCancelled(responsible = theo, from = p1)
            ))
        }

        @Test
        fun execute_shouldFail_whenNoCalendarExists_andNotSaveOrPublish() {
            repo.current = null

            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenAbsenceDoesNotExist_andNotSaveOrPublish() {
            repo.current = AvailabilityCalendar.create(setOf(fabio, theo, charles))

            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenResponsibleNotInRoster_andNotSaveOrPublish() {
            val outsider = Responsible("Outsider")

            repo.current = AvailabilityCalendar.create(setOf(fabio, theo, charles))
                .recordAbsence(theo, p1, periodsCount = 2)

            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(outsider, p1))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }
    }

    class InMemoryAvailabilityCalendarRepository(private val roster: Set<Responsible>) : AvailabilityCalendarRepository {
        var current: AvailabilityCalendar? = null
        var saved: AvailabilityCalendar? = null

        override fun get(): AvailabilityCalendar = current ?: AvailabilityCalendar.create(roster = roster)

        override fun save(calendar: AvailabilityCalendar) {
            current = calendar
            saved = calendar
        }

        fun reset() {
            current = AvailabilityCalendar.create(roster = roster)
            saved = null
        }
    }

    class CapturingEventsPublisher : EventsPublisher {
        val publishedEvents = mutableListOf<DomainEvent>()
        override fun publish(events: List<DomainEvent>) {
            publishedEvents += events
        }
    }
}
