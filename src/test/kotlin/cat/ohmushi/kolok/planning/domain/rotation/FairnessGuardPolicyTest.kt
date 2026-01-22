package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FairnessGuardPolicyTest {

    private val period = Period(LocalDate.of(2026, 1, 12))

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")

    @Test
    fun apply_shouldFail_whenDraftIsNull() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine),
            previous = null
        )

        assertThatThrownBy { FairnessGuardPolicy().apply(request, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldReturnSameDraft_whenValid() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )

        val result = FairnessGuardPolicy().apply(request, draft)

        assertThat(result).isSameAs(draft)
    }

    @Test
    fun apply_shouldFail_whenAnyResponsibilityMissing() {
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

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenDuplicateResponsibilityAssigned() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, cuisine)
            )
        )

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenAssignmentHasUnknownResponsible() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(Responsible("SomeoneElse"), cuisine)
            )
        )

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenAssignmentHasResponsibilityNotInRequest() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio),
            responsibilities = listOf(cuisine),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, toilets)
            )
        )

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenLoadDiffGreaterThanOne() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(fabio, bathroom),
                Assignment(fabio, livingRoom),
                Assignment(theo, toilets)
            )
        )

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldFail_whenResponsibilitiesAtLeastResponsibles_andSomeoneHasZero() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
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

        assertThatThrownBy { FairnessGuardPolicy().apply(request, draft) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldAllowZeroLoad_whenResponsibilitiesLessThanResponsibles() {
        val request = RotationRequest(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom),
            previous = null
        )

        val draft = RotationDraft(
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom)
            )
        )

        val result = FairnessGuardPolicy().apply(request, draft)

        assertThat(result).isSameAs(draft)
    }
}
