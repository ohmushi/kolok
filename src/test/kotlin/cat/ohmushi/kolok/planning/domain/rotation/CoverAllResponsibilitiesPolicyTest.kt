package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CoverAllResponsibilitiesPolicyTest {

    private val period = Period(LocalDate.of(2026, 1, 12))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")

    @Test
    fun apply_shouldReturnDraftUnchanged_whenAlreadyCoversAllResponsibilities() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom)
            )
        )

        val result = CoverAllResponsibilitiesPolicy().apply(request, draft)

        assertThat(result).isEqualTo(draft)
    }

    @Test
    fun apply_shouldAddMissingResponsibilities() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, toilets),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom)
            )
        )

        val result = CoverAllResponsibilitiesPolicy().apply(request, draft)

        assertThat(result.assignments.map { it.responsibility }.toSet())
            .isEqualTo(setOf(cuisine, bathroom, toilets))
    }

    @Test
    fun apply_shouldAssignMissingResponsibilitiesToLeastLoadedResponsible() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(fabio, bathroom)
            )
        )

        val result = CoverAllResponsibilitiesPolicy().apply(request, draft)

        assertThat(result.assignments).contains(Assignment(theo, livingRoom))
    }

    @Test
    fun apply_shouldBeDeterministic_whenTieOnLoad_breakByResponsibleName() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(theo, fabio, charles),
            responsibilities = listOf(cuisine),
            previous = null
        )

        val result = CoverAllResponsibilitiesPolicy().apply(request, RotationDraft(emptyList()))

        assertThat(result.assignments).containsExactly(
            Assignment(charles, cuisine)
        )
    }

    @Test
    fun apply_shouldRemoveAssignmentsForResponsibilitiesNotInRequest() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom)
            )
        )

        val result = CoverAllResponsibilitiesPolicy().apply(request, draft)

        assertThat(result.assignments).containsExactly(Assignment(fabio, cuisine))
    }

    @Test
    fun apply_shouldFail_whenNoResponsibles() {
        val request = RotationRequest(
            period = period,
            responsibles = emptyList(),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { CoverAllResponsibilitiesPolicy().apply(request, RotationDraft(emptyList())) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
