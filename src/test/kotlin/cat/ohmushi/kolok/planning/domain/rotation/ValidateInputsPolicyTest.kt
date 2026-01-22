package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ValidateInputsPolicyTest {

    private val period = Period(LocalDate.of(2026, 1, 12))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")

    @Test
    fun apply_shouldReturnSameDraft_whenInputsAreValid_andDraftIsNull() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val result = ValidateInputsPolicy().apply(request, null)

        assertThat(result.assignments).isEmpty()
    }

    @Test
    fun apply_shouldReturnSameDraftInstance_whenDraftProvided() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val draft = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))

        val result = ValidateInputsPolicy().apply(request, draft)

        assertThat(result).isSameAs(draft)
    }

    @Test
    fun apply_shouldFail_whenResponsiblesEmpty() {
        val request = RotationRequest(
            period = period,
            responsibles = emptyList(),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsibilitiesEmpty() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = emptyList(),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsiblesContainDuplicates() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, fabio),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsibilitiesContainDuplicates() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine, cuisine),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsibleNameBlank() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(Responsible(" ")),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsibilityNameBlank() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = listOf(Responsibility("")),
            previous = null
        )

        assertThatThrownBy { ValidateInputsPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
