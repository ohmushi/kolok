package cat.ohmushi.kolok.planning.domain.rotation

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import kotlin.test.Test

class CompositeRotationPolicyTest {
    private val period = Period(LocalDate.of(2026, 1, 12))
    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val cuisine = Responsibility("Cuisine")

    private fun request() = RotationRequest(
        period = period,
        responsibles = listOf(fabio, theo),
        responsibilities = listOf(cuisine),
        previous = null
    )

    @BeforeEach
    fun setup() {
        CapturingPolicy.reset()
    }

    @Test
    fun apply_shouldFail_whenNoPolicies() {
        val policy = CompositeRotationPolicy(emptyList())

        assertThatThrownBy { policy.apply(request(), null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun apply_shouldApplyPoliciesInOrder_andReturnFinalDraft() {
        val r = request()
        val d1 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))
        val d2 = RotationDraft(assignments = listOf(Assignment(theo, cuisine)))
        val d3 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))

        val p1 = CapturingPolicy("p1", returning = d1)
        val p2 = CapturingPolicy("p2", returning = d2)
        val p3 = CapturingPolicy("p3", returning = d3)

        val composite = CompositeRotationPolicy(listOf(p1, p2, p3))

        val result = composite.apply(r, null)

        assertThat(result).isEqualTo(d3)
        assertThat(p1.called).isEqualTo(1)
        assertThat(p2.called).isEqualTo(1)
        assertThat(p3.called).isEqualTo(1)
        assertThat(p1.callOrder).containsExactly(1)
        assertThat(p2.callOrder).containsExactly(2)
        assertThat(p3.callOrder).containsExactly(3)
    }

    @Test
    fun apply_shouldPassNullDraftToFirstPolicy_andPreviousDraftToNextOnes() {
        val r = request()
        val d1 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))
        val d2 = RotationDraft(assignments = listOf(Assignment(theo, cuisine)))

        val p1 = CapturingPolicy("p1", returning = d1)
        val p2 = CapturingPolicy("p2", returning = d2)

        val composite = CompositeRotationPolicy(listOf(p1, p2))

        composite.apply(r, null)

        assertThat(p1.lastDraft).isNull()
        assertThat(p2.lastDraft).isEqualTo(d1)
    }

    @Test
    fun apply_shouldThreadAnInitialDraftThroughTheChain() {
        val r = request()
        val initial = RotationDraft(assignments = listOf(Assignment(theo, cuisine)))
        val d1 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))
        val d2 = RotationDraft(assignments = listOf(Assignment(theo, cuisine)))

        val p1 = CapturingPolicy("p1", returning = d1)
        val p2 = CapturingPolicy("p2", returning = d2)

        val composite = CompositeRotationPolicy(listOf(p1, p2))

        composite.apply(r, initial)

        assertThat(p1.lastDraft).isEqualTo(initial)
        assertThat(p2.lastDraft).isEqualTo(d1)
    }

    @Test
    fun apply_shouldPassSameRequestInstanceToAllPolicies() {
        val r = request()
        val d1 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))
        val d2 = RotationDraft(assignments = listOf(Assignment(theo, cuisine)))

        val p1 = CapturingPolicy("p1", returning = d1)
        val p2 = CapturingPolicy("p2", returning = d2)

        val composite = CompositeRotationPolicy(listOf(p1, p2))

        composite.apply(r, null)

        assertThat(p1.lastRequest).isSameAs(r)
        assertThat(p2.lastRequest).isSameAs(r)
    }

    @Test
    fun apply_shouldStopAndPropagateException_whenApplyFails() {
        val r = request()
        val d1 = RotationDraft(assignments = listOf(Assignment(fabio, cuisine)))

        val p1 = CapturingPolicy("p1", returning = d1)
        val p2 = FailingPolicy("p2", ex = IllegalArgumentException("boom"))
        val p3 = CapturingPolicy("p3", returning = d1)

        val composite = CompositeRotationPolicy(listOf(p1, p2, p3))

        assertThatThrownBy { composite.apply(r, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("boom")

        assertThat(p1.called).isEqualTo(1)
        assertThat(p3.called).isEqualTo(0)
    }

    private class CapturingPolicy(
        private val name: String,
        private val returning: RotationDraft
    ) : RotationPolicy {

        var called: Int = 0
        var lastRequest: RotationRequest? = null
        var lastDraft: RotationDraft? = null
        val callOrder = mutableListOf<Int>()

        override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
            called += 1
            lastRequest = request
            lastDraft = draft
            callOrder += calledGlobal
            calledGlobal += 1
            return returning
        }

        companion object {
            private var calledGlobal: Int = 1

            fun reset() {
                calledGlobal = 1
            }
        }
    }

    private class FailingPolicy(
        private val name: String,
        private val ex: RuntimeException
    ) : RotationPolicy {

        var called: Int = 0

        override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
            called += 1
            throw ex
        }
    }
}