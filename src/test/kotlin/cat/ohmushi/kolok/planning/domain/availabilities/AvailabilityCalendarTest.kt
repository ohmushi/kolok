package cat.ohmushi.kolok.planning.domain.availabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import cat.ohmushi.kolok.planning.domain.events.AbsenceCancelled
import cat.ohmushi.kolok.planning.domain.events.AbsenceRecorded
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AvailabilityCalendarTest {

    private val p0 = Period(LocalDate.of(2026, 1, 5))
    private val p1 = Period(LocalDate.of(2026, 1, 12))
    private val p2 = Period(LocalDate.of(2026, 1, 19))
    private val p3 = Period(LocalDate.of(2026, 1, 26))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    @Test
    fun create_shouldFail_whenRosterEmpty() {
        assertThatThrownBy { AvailabilityCalendar.create(emptySet()) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun create_shouldFail_whenRosterContainsBlankName() {
        assertThatThrownBy { AvailabilityCalendar.create(setOf(Responsible(" "))) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun availableFor_shouldReturnAllRosterSortedByName_whenNoAbsences() {
        val calendar = AvailabilityCalendar.create(setOf(theo, fabio, charles))

        val available = calendar.availableFor(p0)

        assertThat(available).containsExactly(theo, fabio, charles)
    }

    @Test
    fun recordAbsence_shouldFail_whenResponsibleNotInRoster() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))

        assertThatThrownBy { calendar.recordAbsence(charles, p1, periodsCount = 1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun recordAbsence_shouldFail_whenPeriodsCountInvalid() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))

        assertThatThrownBy { calendar.recordAbsence(theo, p1, periodsCount = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun recordAbsence_shouldRemoveResponsibleFromAvailability_inInclusiveRange() {
        val calendar = AvailabilityCalendar.create(setOf(charles, fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)

        assertThat(calendar.availableFor(p0)).containsExactly(charles, fabio, theo)
        assertThat(calendar.availableFor(p1)).containsExactly(charles, fabio)
        assertThat(calendar.availableFor(p2)).containsExactly(charles, fabio)
        assertThat(calendar.availableFor(p3)).containsExactly(charles, fabio, theo)
    }

    @Test
    fun recordAbsence_shouldBeAdditive_whenMultipleAbsences() {
        val calendar = AvailabilityCalendar.create(setOf(charles, fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)
            .recordAbsence(charles, p2, periodsCount = 2)

        assertThat(calendar.availableFor(p1)).containsExactly(charles, fabio)
        assertThat(calendar.availableFor(p2)).containsExactly(fabio)
        assertThat(calendar.availableFor(p3)).containsExactly(fabio, theo)
    }

    @Test
    fun recordAbsence_shouldBeIdempotent_whenSameAbsenceAlreadyRecorded() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)

        val (afterFirstClean, firstEvents) = calendar.consumeEvents()
        assertThat(firstEvents).containsExactly(AbsenceRecorded(responsible = theo, from = p1, periodsCount = 2))

        val again = afterFirstClean.recordAbsence(theo, p1, periodsCount = 2)

        assertThat(again.snapshotAbsences()).containsExactly(Absence(theo, p1, periodsCount = 2))

        val (_, eventsAfterSecond) = again.consumeEvents()
        assertThat(eventsAfterSecond).isEmpty()
    }

    @Test
    fun cancelAbsence_shouldFail_whenResponsibleNotInRoster() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))

        assertThatThrownBy { calendar.cancelAbsence(charles, p1, periodsCount = 1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun cancelAbsence_shouldFail_whenPeriodsCountInvalid() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))

        assertThatThrownBy { calendar.cancelAbsence(theo, p1, periodsCount = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun cancelAbsence_shouldFail_whenAbsenceDoesNotExist() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))

        assertThatThrownBy { calendar.cancelAbsence(theo, p1, periodsCount = 2) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun cancelAbsence_shouldRestoreAvailability() {
        val calendar = AvailabilityCalendar.create(setOf(charles, fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)

        val restored = calendar.cancelAbsence(theo, p1, periodsCount = 2)

        assertThat(restored.availableFor(p1)).containsExactly(charles, fabio, theo)
        assertThat(restored.availableFor(p2)).containsExactly(charles, fabio, theo)
    }

    @Test
    fun snapshotAbsences_shouldExposeCurrentAbsences() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)

        val absences = calendar.snapshotAbsences()

        assertThat(absences).containsExactly(Absence(theo, p1, periodsCount = 2))
    }

    @Test
    fun consumeEvents_shouldContainEventsRecorded_andCancelled() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)
            .cancelAbsence(theo, p1, periodsCount = 2)

        val (clean, events) = calendar.consumeEvents()

        assertThat(events).hasSize(2)
        assertThat(events[0]).isEqualTo(AbsenceRecorded(responsible = theo, from = p1, periodsCount = 2))
        assertThat(events[1]).isEqualTo(AbsenceCancelled(responsible = theo, from = p1, periodsCount = 2))

        assertThat(clean.consumeEvents().second).isEmpty()
    }

    @Test
    fun recordAbsenceOnAlreadyExistingAbsence_shouldNotDuplicateAbsence() {
        val calendar = AvailabilityCalendar.create(setOf(fabio, theo))
            .recordAbsence(theo, p1, periodsCount = 2)

        val updated = calendar.recordAbsence(theo, p1.next(), periodsCount = 1)

        assertThat(updated.snapshotAbsences()).containsExactly(Absence(theo, p1, periodsCount = 2))
    }
}
