package cat.ohmushi.kolok.planning.domain

import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest
import cat.ohmushi.kolok.planning.domain.rotation.ShiftByOneRotationPolicy
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.collections.emptyList

class ShiftByOnePolicyTest {

    private val period = Period(LocalDate.of(2026, 1, 12))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")
    private val trash = Responsibility("Poubelles")

    @Test
    fun apply_shouldAssignEachResponsibilityToNextResponsibleInCycle_3responsibles3responsibilities() {
        val previous = Planning(
            period = Period(LocalDate.of(2026, 1, 5)),
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )

        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = previous
        )

        val draft = ShiftByOneRotationPolicy().apply(request)

        assertThat(draft.assignments).containsExactlyInAnyOrder(
            Assignment(theo, cuisine),
            Assignment(charles, bathroom),
            Assignment(fabio, livingRoom)
        )
    }

    @Test
    fun apply_shouldShiftEvenWhenOneResponsibleHadTwoResponsibilities() {
        val previous = Planning(
            period = Period(LocalDate.of(2026, 1, 5)),
            responsibles = listOf(theo, charles, fabio),
            responsibilities = listOf(bathroom, livingRoom, cuisine, toilets, trash),
            assignments = listOf(
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom),
                Assignment(fabio, cuisine),
                Assignment(theo, toilets),
                Assignment(charles, trash)
            )
        )

        val request = RotationRequest(
            period = period,
            responsibles = listOf(theo, charles, fabio),
            responsibilities = listOf(bathroom, livingRoom, cuisine, toilets, trash),
            previous = previous
        )

        val draft = ShiftByOneRotationPolicy().apply(request)

        assertThat(draft.assignments).containsExactlyInAnyOrder(
            Assignment(charles, bathroom),
            Assignment(fabio, livingRoom),
            Assignment(theo, cuisine),
            Assignment(charles, toilets),
            Assignment(fabio, trash)
        )
    }

    @Test
    fun apply_shouldBeDeterministic_givenSameInputs() {
        val previous = Planning(
            period = Period(LocalDate.of(2026, 1, 5)),
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )

        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = previous
        )

        val d1 = ShiftByOneRotationPolicy().apply(request)
        val d2 = ShiftByOneRotationPolicy().apply(request)

        assertThat(d1).isEqualTo(d2)
    }

    @Test
    fun apply_shouldFail_whenNoPreviousPlanning() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )


        assertThatThrownBy { ShiftByOneRotationPolicy().apply(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsiblesListIsEmpty() {
        val previous = Planning(
            period = Period(LocalDate.of(2026, 1, 5)),
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine),
            assignments = listOf(Assignment(fabio, cuisine))
        )

        val request = RotationRequest(
            period = period,
            responsibles = emptyList(),
            responsibilities = listOf(cuisine),
            previous = previous
        )

        val policy = ShiftByOneRotationPolicy()

        assertThatThrownBy { policy.apply(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
