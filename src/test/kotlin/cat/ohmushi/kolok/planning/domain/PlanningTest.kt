package cat.ohmushi.kolok.planning.domain

import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PlanningTest {

    @Nested
    inner class PlanningCreationTest {

        private val period = Period(LocalDate.of(2026, 1, 12))
        private val fabio = Responsible("Fabio")
        private val theo = Responsible("Theo")
        private val charles = Responsible("Charles")

        private val cuisine = Responsibility("Cuisine")
        private val bathroom = Responsibility("Salle de bain")
        private val livingRoom = Responsibility("Salon")
        private val toilets = Responsibility("Toilettes")

        @Test
        fun createsPlanning_withValidAssignments_shouldSucceed() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(charles, livingRoom)
                )
            )

            assertThat(planning.assignments).hasSize(3)
        }


        @Test
        fun createPlanning_withNoResponsibles_shouldFail() {
            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = emptyList(),
                    responsibilities = listOf(cuisine),
                    assignments = emptyList()
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_withNoResponsibilities_shouldFail() {
            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio),
                    responsibilities = emptyList(),
                    assignments = emptyList()
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_missingAResponsibility_shouldFail() {
            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio, theo),
                    responsibilities = listOf(cuisine, bathroom),
                    assignments = listOf(
                        Assignment(fabio, cuisine)
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_withDuplicateResponsibilityAssignedTwice_shouldFail() {
            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio, theo),
                    responsibilities = listOf(cuisine),
                    assignments = listOf(
                        Assignment(fabio, cuisine),
                        Assignment(theo, cuisine)
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_assignmentWithUnknownResponsible_shouldFail() {
            val unknown = Responsible("Unknown")

            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio),
                    responsibilities = listOf(cuisine),
                    assignments = listOf(
                        Assignment(unknown, cuisine)
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_assignmentWithUnknownResponsibility_shouldFail() {
            val unknown = Responsibility("Unknown")

            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio),
                    responsibilities = listOf(cuisine),
                    assignments = listOf(
                        Assignment(fabio, unknown)
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_unfairLoadDifferenceGreaterThanOne_shouldFail() {
            assertThatThrownBy {
                Planning(
                    period = period,
                    responsibles = listOf(fabio, theo, charles),
                    responsibilities = listOf(cuisine, bathroom, livingRoom, toilets),
                    assignments = listOf(
                        Assignment(fabio, cuisine),
                        Assignment(fabio, bathroom),
                        Assignment(fabio, livingRoom),
                        Assignment(theo, toilets)
                    )
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun createPlanning_whenResponsibilitiesAtLeastResponsibles_noOneHasZero_shouldHold() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(charles, livingRoom)
                )
            )

            val loads = planning.assignments.groupBy { it.responsible }.mapValues { it.value.size }
            assertThat(loads.values).doesNotContain(0)
        }

        @Test
        fun createPlanning_whenResponsibilitiesLessThanResponsibles_someMayHaveZero_shouldSucceed() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom)
                )
            )

            assertThat(planning.assignments).hasSize(2)
        }
    }

    @Nested
    inner class PlanningQueriesTest {

        private val period = Period(LocalDate.of(2026, 1, 12))
        private val fabio = Responsible("Fabio")
        private val theo = Responsible("Theo")
        private val charles = Responsible("Charles")

        private val cuisine = Responsibility("Cuisine")
        private val bathroom = Responsibility("Salle de bain")
        private val livingRoom = Responsibility("Salon")

        val planning = Planning(
            period = period,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )


        @Test
        fun getAssignmentsForResponsible_shouldReturnOnlyTheirAssignments() {

            val result = planning.assignmentsFor(fabio)

            assertThat(result).containsExactly(Assignment(fabio, cuisine))
        }

        @Test
        fun getAssignmentsForResponsible_whenResponsibleHasNone_shouldReturnEmpty() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom)
                )
            )

            val result = planning.assignmentsFor(charles)

            assertThat(result).isEmpty()
        }

        @Test
        fun getLoadPerResponsible_shouldMatchAssignmentsCounts() {
            val loads = planning.loadPerResponsible()

            assertThat(loads).containsExactlyInAnyOrderEntriesOf(
                mapOf(
                    fabio to 1,
                    theo to 1,
                    charles to 1
                )
            )
        }

        @Test
        fun getLoadPerResponsible_whenSomeHaveZero_shouldIncludeZero() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom)
                )
            )

            val loads = planning.loadPerResponsible()

            assertThat(loads[fabio]).isEqualTo(1)
            assertThat(loads[theo]).isEqualTo(1)
            assertThat(loads[charles]).isEqualTo(0)
        }

    }

    @Nested
    inner class PlanningResponsibilitiesEvolutionTest {

        private val period = Period(LocalDate.of(2026, 1, 12))
        private val fabio = Responsible("Fabio")
        private val theo = Responsible("Theo")
        private val charles = Responsible("Charles")

        private val cuisine = Responsibility("Cuisine")
        private val bathroom = Responsibility("Salle de bain")
        private val livingRoom = Responsibility("Salon")
        private val toilets = Responsibility("Toilettes")

        private fun basePlanning3x3(): Planning {
            return Planning(
                period = period,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(charles, livingRoom)
                )
            )
        }

        private fun maxLoadDiff(planning: Planning): Int {
            val loads = planning.loadPerResponsible().values.toList()
            if (loads.isEmpty()) return 0
            return loads.maxOrNull()!! - loads.minOrNull()!!
        }

        @Test
        fun addResponsibility_shouldReturnNewPlanning_andKeepOriginalUnchanged() {
            val planning = basePlanning3x3()

            val updated = planning.addResponsibility(toilets)

            assertThat(planning.responsibilities).containsExactlyInAnyOrder(cuisine, bathroom, livingRoom)
            assertThat(updated.responsibilities).containsExactlyInAnyOrder(cuisine, bathroom, livingRoom, toilets)
            assertThat(planning.assignments).hasSize(3)
            assertThat(updated.assignments).hasSize(4)
        }

        @Test
        fun addResponsibility_shouldAssignIt_andRemainValidAndFair() {
            val planning = basePlanning3x3()

            val updated = planning.addResponsibility(toilets)

            assertThat(updated.responsibilities).contains(toilets)
            assertThat(updated.assignments.map { it.responsibility }).contains(toilets)
            assertThat(updated.assignments.map { it.responsibility }.toSet()).isEqualTo(updated.responsibilities.toSet())
            assertThat(maxLoadDiff(updated)).isLessThanOrEqualTo(1)
        }

        @Test
        fun addResponsibility_duplicate_shouldFail() {
            val planning = basePlanning3x3()

            assertThatThrownBy { planning.addResponsibility(cuisine) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun removeResponsibility_shouldReturnNewPlanning_andKeepOriginalUnchanged() {
            val planning = basePlanning3x3()

            val updated = planning.removeResponsibility(bathroom)

            assertThat(planning.responsibilities).containsExactlyInAnyOrder(cuisine, bathroom, livingRoom)
            assertThat(updated.responsibilities).containsExactlyInAnyOrder(cuisine, livingRoom)
            assertThat(planning.assignments).hasSize(3)
            assertThat(updated.assignments).hasSize(2)
            assertThat(updated.assignments.map { it.responsibility }).doesNotContain(bathroom)
        }

        @Test
        fun removeResponsibility_shouldRemainValidAndFair() {
            val planning = basePlanning3x3()

            val updated = planning.removeResponsibility(bathroom)

            assertThat(updated.assignments.map { it.responsibility }.toSet()).isEqualTo(updated.responsibilities.toSet())
            assertThat(maxLoadDiff(updated)).isLessThanOrEqualTo(1)
        }

        @Test
        fun removeResponsibility_unknown_shouldFail() {
            val planning = basePlanning3x3()
            val unknown = Responsibility("Unknown")

            assertThatThrownBy { planning.removeResponsibility(unknown) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun addResponsibility_withFewerResponsiblesThanResponsibilities_shouldStillBeFair() {
            val planning = Planning(
                period = period,
                responsibles = listOf(fabio, theo),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(fabio, livingRoom)
                )
            )

            val updated = planning.addResponsibility(toilets)

            assertThat(updated.assignments).hasSize(4)
            assertThat(updated.assignments.map { it.responsibility }.toSet()).isEqualTo(updated.responsibilities.toSet())
            assertThat(maxLoadDiff(updated)).isLessThanOrEqualTo(1)
        }

        @Test
        fun removeResponsibility_whenResponsibilitiesBecomeLessThanResponsibles_shouldStillBeValid() {
            val planning = basePlanning3x3()

            val updated = planning
                .removeResponsibility(cuisine)

            assertThat(updated.responsibilities).containsExactlyInAnyOrder(bathroom, livingRoom)
            assertThat(updated.assignments.map { it.responsibility }.toSet()).isEqualTo(updated.responsibilities.toSet())
            assertThat(maxLoadDiff(updated)).isLessThanOrEqualTo(1)
        }
    }

}