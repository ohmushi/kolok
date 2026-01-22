package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.CancelAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.RecordAbsenceUseCase
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.availability.AvailabilityCalendar
import cat.ohmushi.kolok.planning.domain.events.AbsenceCancelled
import cat.ohmushi.kolok.planning.domain.events.AbsenceRecorded
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

val p1 = Period(LocalDate.of(2026, 1, 12))
val p2 = Period(LocalDate.of(2026, 1, 19))
val p3 = Period(LocalDate.of(2026, 1, 26))

val fabio = Responsible("Fabio")
val theo = Responsible("Theo")
val charles = Responsible("Charles")

class AvailabilityServiceTest {



    abstract class Base {
        protected val publisher = CapturingEventPublisher()
        protected val repo = InMemoryAvailabilityCalendarRepository()

        protected var service = AvailabilityService(
            availabilityCalendarRepository = repo,
            eventPublisher = publisher
        )

        protected fun recordUseCase(): RecordAbsenceUseCase = service
        protected fun cancelUseCase(): CancelAbsenceUseCase = service
    }

    @Nested
    inner class RecordAbsenceUseCaseTest : Base() {

        @BeforeEach
        fun setUp() {
            repo.reset()
            publisher.publishedEvents.clear()
        }

        @Test
        fun execute_shouldCreateCalendarIfMissing_recordAbsence_save_andPublishEvent() {

            recordUseCase().recordAbsence(
                RecordAbsenceCommand(
                    responsible = theo,
                    from = p1,
                    to = p2
                )
            )

            assertThat(repo.saved).isNotNull
            assertThat(repo.saved!!.availableFor(p1)).containsExactly(charles, fabio)
            assertThat(repo.saved!!.availableFor(p2)).containsExactly(charles, fabio)
            assertThat(repo.saved!!.availableFor(p3)).containsExactly(charles, fabio, theo)

            assertThat(publisher.publishedEvents).containsExactly(
                AbsenceRecorded(responsible = theo, from = p1, to = p2)
            )
        }

        @Test
        fun execute_shouldFail_whenResponsibleNotInRoster_andNotSaveOrPublish() {
            val outsider = Responsible("Outsider")

            assertThatThrownBy {
                recordUseCase().recordAbsence(
                    RecordAbsenceCommand(
                        responsible = outsider,
                        from = p1,
                        to = p2
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenFromAfterTo_andNotSaveOrPublish() {
            assertThatThrownBy {
                recordUseCase().recordAbsence(
                    RecordAbsenceCommand(
                        responsible = theo,
                        from = p2,
                        to = p1
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenSameAbsenceAlreadyRecorded_andNotSaveOrPublishSecondTime() {
            recordUseCase().recordAbsence(RecordAbsenceCommand(theo, p1, p2))

            publisher.publishedEvents.clear()
            repo.saved = null

            assertThatThrownBy {
                recordUseCase().recordAbsence(RecordAbsenceCommand(theo, p1, p2))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }
    }

    @Nested
    inner class CancelAbsenceUseCaseTest : Base() {

        @BeforeEach
        fun setUp() {
            repo.reset()
            publisher.publishedEvents.clear()
        }

        @Test
        fun execute_shouldCancelAbsence_save_andPublishEvent() {
            recordUseCase().recordAbsence(RecordAbsenceCommand(theo, p1, p2))
            val before = publisher.publishedEvents.toList()

            cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1, p2))

            assertThat(repo.saved).isNotNull
            assertThat(repo.saved!!.availableFor(p1)).containsExactly(charles, fabio, theo)
            assertThat(repo.saved!!.availableFor(p2)).containsExactly(charles, fabio, theo)

            assertThat(publisher.publishedEvents).containsExactlyElementsOf(before + listOf(
                AbsenceCancelled(responsible = theo, from = p1, to = p2)
            ))
        }

        @Test
        fun execute_shouldFail_whenNoCalendarExists_andNotSaveOrPublish() {
            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1, p2))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenAbsenceDoesNotExist_andNotSaveOrPublish() {
            repo.current = AvailabilityCalendar.create(setOf(fabio, theo, charles))

            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(theo, p1, p2))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }

        @Test
        fun execute_shouldFail_whenResponsibleNotInRoster_andNotSaveOrPublish() {
            val outsider = Responsible("Outsider")

            repo.current = AvailabilityCalendar.create(setOf(fabio, theo, charles))
                .recordAbsence(theo, p1, p2)

            assertThatThrownBy {
                cancelUseCase().cancelAbsence(CancelAbsenceCommand(outsider, p1, p2))
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isNull()
            assertThat(publisher.publishedEvents).isEmpty()
        }
    }

    class InMemoryAvailabilityCalendarRepository : AvailabilityCalendarRepository {
        var current: AvailabilityCalendar? = null
        var saved: AvailabilityCalendar? = null

        override fun get(): AvailabilityCalendar? = current

        override fun save(calendar: AvailabilityCalendar) {
            current = calendar
            saved = calendar
        }

        fun reset() {
            current = AvailabilityCalendar.create(roster = setOf(fabio, theo, charles))
            saved = null
        }
    }

    class CapturingEventPublisher : EventPublisher {
        val publishedEvents = mutableListOf<DomainEvent>()
        override fun publish(events: List<DomainEvent>) {
            publishedEvents += events
        }
    }
}
