package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import kotlin.test.Test

class ProjectPreviousAssignmentsPolicyTest {

    private val periodS = Period(LocalDate.of(2026, 1, 12))
    private val periodSMinus1 = Period(LocalDate.of(2026, 1, 5))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")
    private val diane = Responsible("Diane")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")

    @Test
    fun apply_shouldReturnIncomingDraft_whenDraftIsProvided() {
        val incoming = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine)
            )
        )

        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val result = ProjectPreviousAssignmentsPolicy().apply(request, incoming)

        assertThat(result).isSameAs(incoming)
    }

    @Test
    fun apply_shouldReturnEmptyDraft_whenNoPreviousPlanning_andNoDraft() {
        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val result = ProjectPreviousAssignmentsPolicy().apply(request, null)

        assertThat(result.assignments).isEmpty()
    }

    @Test
    fun apply_shouldKeepOnlyAssignmentsWithActiveResponsiblesAndActiveResponsibilities() {
        val previous = Planning(
            period = periodSMinus1,
            responsibles = listOf(fabio, theo, charles, diane),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom),
                Assignment(diane, toilets)
            )
        )

        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = previous
        )

        val result = ProjectPreviousAssignmentsPolicy().apply(request, null)

        assertThat(result.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, cuisine),
            Assignment(theo, bathroom)
        )
    }

    @Test
    fun apply_shouldFilterOutAssignmentsForRemovedResponsibilities() {
        val previous = Planning(
            period = periodSMinus1,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )

        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom),
            previous = previous
        )

        val result = ProjectPreviousAssignmentsPolicy().apply(request, null)

        assertThat(result.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, cuisine),
            Assignment(theo, bathroom)
        )
        assertThat(result.assignments.map { it.responsibility }).doesNotContain(livingRoom)
    }

    @Test
    fun apply_shouldReturnOnlyMatchingAssignments_evenIfPreviousContainsExtraData() {
        val previous = Planning(
            period = periodSMinus1,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom),
                Assignment(fabio, toilets)
            )
        )

        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = previous
        )

        val result = ProjectPreviousAssignmentsPolicy().apply(request, null)

        assertThat(result.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, cuisine),
            Assignment(theo, bathroom)
        )
    }
}