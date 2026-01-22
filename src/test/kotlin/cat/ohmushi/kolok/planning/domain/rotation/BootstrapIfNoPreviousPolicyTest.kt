package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BootstrapIfNoPreviousPolicyTest {

    private val period = Period(LocalDate.of(2026, 1, 12))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")

    @Test
    fun apply_shouldReturnDraftUnchanged_whenDraftIsNonEmpty() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine)
            )
        )

        val result = BootstrapIfNoPreviousPolicy().apply(request, draft)

        assertThat(result).isSameAs(draft)
    }

    @Test
    fun apply_shouldBootstrap_whenNoPrevious_andDraftIsNull() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )

        val result = BootstrapIfNoPreviousPolicy().apply(request, null)

        assertThat(result.assignments).hasSize(3)
        assertThat(result.assignments.map { it.responsibility }.toSet())
            .isEqualTo(setOf(cuisine, bathroom, livingRoom))
        assertThat(result.assignments.map { it.responsible }.toSet())
            .isSubsetOf(setOf(fabio, theo, charles))
    }

    @Test
    fun apply_shouldBootstrap_whenNoPrevious_andDraftIsEmpty() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, toilets),
            previous = null
        )

        val result = BootstrapIfNoPreviousPolicy().apply(request, RotationDraft(emptyList()))

        assertThat(result.assignments).hasSize(3)
        assertThat(result.assignments.map { it.responsibility }.toSet())
            .isEqualTo(setOf(cuisine, bathroom, toilets))
    }

    @Test
    fun apply_shouldDistributeRoundRobin_whenNoPrevious() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
            previous = null
        )

        val result = BootstrapIfNoPreviousPolicy().apply(request, null)

        assertThat(result.assignments).containsExactly(
            Assignment(fabio, cuisine),
            Assignment(theo, bathroom),
            Assignment(fabio, livingRoom),
            Assignment(theo, toilets)
        )
    }

    @Test
    fun apply_shouldBeDeterministic_forSameRequest() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
            previous = null
        )

        val policy = BootstrapIfNoPreviousPolicy()

        val r1 = policy.apply(request, null)
        val r2 = policy.apply(request, null)

        assertThat(r1).isEqualTo(r2)
    }

    @Test
    fun apply_shouldReturnEmptyDraft_whenPreviousExists_andDraftIsNull() {
        val previous = cat.ohmushi.kolok.planning.domain.Planning(
            period = Period(LocalDate.of(2026, 1, 5)),
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine),
            assignments = listOf(Assignment(fabio, cuisine))
        )

        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = previous
        )

        val result = BootstrapIfNoPreviousPolicy().apply(request, null)

        assertThat(result.assignments).isEmpty()
    }
}
