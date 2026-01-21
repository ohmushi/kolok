package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.Assignment
import cat.ohmushi.kolok.planning.domain.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Planning
import cat.ohmushi.kolok.planning.domain.PlanningFactory
import cat.ohmushi.kolok.planning.domain.PlanningGenerated
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.rotation.RotationDraft
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.RotationRequest
import org.junit.jupiter.api.Nested
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate



class PlanningServiceTest {

    @Nested
    inner class GeneratePlanningUseCaseTest {

        private val periodS = Period(LocalDate.of(2026, 1, 12))
        private val periodSMinus1 = Period(LocalDate.of(2026, 1, 5))

        private val fabio = Responsible("Fabio")
        private val theo = Responsible("Theo")
        private val charles = Responsible("Charles")

        private val cuisine = Responsibility("Cuisine")
        private val bathroom = Responsibility("Salle de bain")
        private val livingRoom = Responsibility("Salon")

        @Test
        fun execute_shouldLoadPreviousPlanning_buildRequest_applyPolicy_buildPlanning_saveAndPublishEvents() {
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

            val draft = RotationDraft(
                assignments = listOf(
                    Assignment(theo, cuisine),
                    Assignment(charles, bathroom),
                    Assignment(fabio, livingRoom)
                )
            )

            val expectedPlanning = Planning(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = draft.assignments
            )

            val repo = FakePlanningRepository(previous = previous)
            val policy = CapturingRotationPolicy(resultDraft = draft)
            val factory = CapturingPlanningFactory(resultPlanning = expectedPlanning)
            val publisher = CapturingEventPublisher()


            val command = GeneratePlanningCommand(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom)
            )

            val result = PlanningService(
                planningRepository = repo,
                rotationPolicy = policy,
                planningFactory = factory,
                eventPublisher = publisher
            ).generatePlanning(command)

            assertThat(repo.lastFindLatestBeforeArg).isEqualTo(periodS)
            assertThat(policy.lastRequest).isNotNull
            assertThat(policy.lastRequest!!.period).isEqualTo(periodS)
            assertThat(policy.lastRequest!!.previous).isEqualTo(previous)

            assertThat(factory.lastRequest).isNotNull
            assertThat(factory.lastDraft).isEqualTo(draft)
            assertThat(repo.saved).containsExactly(expectedPlanning)

            assertThat(publisher.publishedEvents).isNotEmpty
            assertThat(result.planning).isEqualTo(expectedPlanning)
            assertThat(result.events).isEqualTo(publisher.publishedEvents)
        }

        @Test
        fun execute_whenNoPreviousPlanning_shouldStillGeneratePlanning() {
            val draft = RotationDraft(
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(charles, livingRoom)
                )
            )

            val expectedPlanning = Planning(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = draft.assignments
            )

            val repo = FakePlanningRepository(previous = null)
            val policy = CapturingRotationPolicy(resultDraft = draft)
            val factory = CapturingPlanningFactory(resultPlanning = expectedPlanning)
            val publisher = CapturingEventPublisher()

            val useCase = PlanningService(
                planningRepository = repo,
                rotationPolicy = policy,
                planningFactory = factory,
                eventPublisher = publisher
            )

            val command = GeneratePlanningCommand(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom)
            )

            val result = useCase.generatePlanning(command)

            assertThat(policy.lastRequest!!.previous).isNull()
            assertThat(result.planning).isEqualTo(expectedPlanning)
            assertThat(repo.saved).containsExactly(expectedPlanning)
            assertThat(publisher.publishedEvents).isNotEmpty
        }

        @Test
        fun execute_shouldPublishPlanningGeneratedEventForTargetPeriod() {
            val draft = RotationDraft(
                assignments = listOf(
                    Assignment(fabio, cuisine),
                    Assignment(theo, bathroom),
                    Assignment(charles, livingRoom)
                )
            )

            val planning = Planning(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom),
                assignments = draft.assignments
            )

            val repo = FakePlanningRepository(previous = null)
            val policy = CapturingRotationPolicy(resultDraft = draft)
            val factory = CapturingPlanningFactory(resultPlanning = planning)
            val publisher = CapturingEventPublisher()

            val useCase = PlanningService(
                planningRepository = repo,
                rotationPolicy = policy,
                planningFactory = factory,
                eventPublisher = publisher
            )

            val command = GeneratePlanningCommand(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom)
            )

            useCase.generatePlanning(command)

            assertThat(publisher.publishedEvents.any { it is PlanningGenerated && it.period == periodS }).isTrue()
        }

        @Test
        fun execute_shouldFail_whenRotationPolicyFails_andNotSaveOrPublish() {
            val repo = FakePlanningRepository(previous = null)
            val policy = object : RotationPolicy {
                override fun apply(request: RotationRequest): RotationDraft {
                    throw IllegalArgumentException("rotation failed")
                }
            }
            val factory = CapturingPlanningFactory(
                resultPlanning = Planning(
                    period = periodS,
                    responsibles = listOf(fabio, theo, charles),
                    responsibilities = listOf(cuisine, bathroom, livingRoom),
                    assignments = listOf(
                        Assignment(fabio, cuisine),
                        Assignment(theo, bathroom),
                        Assignment(charles, livingRoom)
                    )
                )
            )
            val publisher = CapturingEventPublisher()

            val useCase = PlanningService(
                planningRepository = repo,
                rotationPolicy = policy,
                planningFactory = factory,
                eventPublisher = publisher
            )

            val command = GeneratePlanningCommand(
                period = periodS,
                responsibles = listOf(fabio, theo, charles),
                responsibilities = listOf(cuisine, bathroom, livingRoom)
            )

            assertThatThrownBy { useCase.generatePlanning(command) }
                .isInstanceOf(IllegalArgumentException::class.java)

            assertThat(repo.saved).isEmpty()
            assertThat(publisher.publishedEvents).isEmpty()
        }
    }

    private class FakePlanningRepository(
        private val previous: Planning?
    ) : PlanningRepository {

        var lastFindLatestBeforeArg: Period? = null
        val saved = mutableListOf<Planning>()

        override fun findLatestBefore(period: Period): Planning? {
            lastFindLatestBeforeArg = period
            return previous
        }

        override fun save(planning: Planning) {
            saved += planning
        }
    }

    private class CapturingRotationPolicy(
        private val resultDraft: RotationDraft
    ) : RotationPolicy {

        var lastRequest: RotationRequest? = null

        override fun apply(request: RotationRequest): RotationDraft {
            lastRequest = request
            return resultDraft
        }
    }

    private class CapturingPlanningFactory(
        private val resultPlanning: Planning
    ) : PlanningFactory {

        var lastRequest: RotationRequest? = null
        var lastDraft: RotationDraft? = null

        override fun from(request: RotationRequest, draft: RotationDraft): Planning {
            lastRequest = request
            lastDraft = draft
            return resultPlanning
        }
    }

    private class CapturingEventPublisher : EventPublisher {

        val publishedEvents = mutableListOf<DomainEvent>()

        override fun publish(events: List<DomainEvent>) {
            publishedEvents += events
        }
    }

}