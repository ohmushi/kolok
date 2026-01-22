package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BalanceLoadPolicyTest {

    private val periodS = Period(LocalDate.of(2026, 1, 12))
    private val periodSMinus1 = Period(LocalDate.of(2026, 1, 5))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")
    private val trash = Responsibility("Poubelles")

    @Test
    fun apply_shouldFail_whenDraftIsNull() {
        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { BalanceLoadPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldReturnSameDraft_whenAlreadyBalancedAndMatchesTargets() {
        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(fabio, bathroom),
                Assignment(theo, livingRoom)
            )
        )

        val result = BalanceLoadPolicy().apply(request, draft)

        assertThat(result).isSameAs(draft)
    }

    @Test
    fun apply_shouldRebalance_whenDiffGreaterThanOne_withoutPrevious() {
        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(fabio, bathroom),
                Assignment(fabio, livingRoom)
            )
        )

        val result = BalanceLoadPolicy().apply(request, draft)

        assertThat(result.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, bathroom),
            Assignment(fabio, livingRoom),
            Assignment(theo, cuisine)
        )
    }

    @Test
    fun apply_shouldRotateSurplus_basedOnPreviousLoads() {
        val previous = Planning(
            period = periodSMinus1,
            responsibles = listOf(theo, charles, fabio),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets, trash),
            assignments = listOf(
                Assignment(theo, bathroom),
                Assignment(theo, toilets),
                Assignment(charles, livingRoom),
                Assignment(charles, trash),
                Assignment(fabio, cuisine)
            )
        )

        val request = RotationRequest(
            period = periodS,
            responsibles = listOf(theo, charles, fabio),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets, trash),
            previous = previous
        )

        val draft = RotationDraft(assignments = previous.assignments)

        val result = BalanceLoadPolicy().apply(request, draft)

        assertThat(result.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, cuisine),
            Assignment(fabio, bathroom),
            Assignment(theo, toilets),
            Assignment(charles, livingRoom),
            Assignment(charles, trash)
        )
    }
}
