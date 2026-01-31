package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.adapters.out.persistence.planning.inmemory.InMemoryPlanningRepository
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.application.ports.out.EventsPublisher
import cat.ohmushi.kolok.planning.bootstrap.Wiring
import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.planning.DefaultPlanningFactory
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning
import cat.ohmushi.kolok.planning.domain.events.PlanningGenerated
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import cat.ohmushi.kolok.planning.domain.rotation.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GeneratePlanningScenarioTest {

    val wiring = Wiring("back/src/test/resources/persistence")
    val rotationPolicy: RotationPolicy = wiring.rotationPolity()

    private val fabio = Responsible("Fabio")
    private val theo = Responsible("Theo")
    private val charles = Responsible("Charles")

    private val cuisine = Responsibility("Cuisine")
    private val bathroom = Responsibility("Salle de bain")
    private val livingRoom = Responsibility("Salon")
    private val toilets = Responsibility("Toilettes")
    private val trash = Responsibility("Poubelles")

    @Test
    fun rotation_nominale_3_responsables_3_responsabilites_sur_3_periodes() {
        val p0 = Period(LocalDate.of(2026, 1, 5))
        val p1 = Period(LocalDate.of(2026, 1, 12))
        val p2 = Period(LocalDate.of(2026, 1, 19))

        val repo = InMemoryPlanningRepository()
        val responsiblesPort = FixedAvailableResponsiblesPort(listOf(fabio, theo, charles))
        val responsibilitiesPort = FixedActiveResponsibilitiesPort(listOf(cuisine, bathroom, livingRoom))
        val publisher = CapturingEventsPublisher()

        val factory = DefaultPlanningFactory()

        val service = PlanningService(
            planningRepository = repo,
            availableResponsiblesPort = responsiblesPort,
            activeResponsibilitiesPort = responsibilitiesPort,
            rotationPolicy = rotationPolicy,
            planningFactory = factory,
            eventsPublisher = publisher
        )

        val initial = Planning(
            period = p0,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom),
                Assignment(fabio, cuisine),
            )
        )
        repo.save(initial)

        val r1 = service.generatePlanning(GeneratePlanningCommand(period = p1)).planning
        assertThat(r1.assignments).containsExactlyInAnyOrder(
            Assignment(theo, cuisine),
            Assignment(charles, bathroom),
            Assignment(fabio, livingRoom),
        )

        val r2 = service.generatePlanning(GeneratePlanningCommand(period = p2)).planning
        assertThat(r2.assignments).containsExactlyInAnyOrder(
            Assignment(theo, livingRoom),
            Assignment(charles, cuisine),
            Assignment(fabio, bathroom),
        )

        val generatedPeriods = publisher.publishedEvents
            .filterIsInstance<PlanningGenerated>()
            .map { it.period }

        assertThat(generatedPeriods).containsExactly(p1, p2)
    }

    @Test
    fun surplus_5_responsabilites_3_responsables_sur_2_periodes_rotation_du_surplus() {
        val p0 = Period(LocalDate.of(2026, 1, 5))
        val p1 = Period(LocalDate.of(2026, 1, 12))

        val repo = InMemoryPlanningRepository()
        val responsiblesPort = FixedAvailableResponsiblesPort(listOf(fabio, theo, charles))
        val responsibilitiesPort = FixedActiveResponsibilitiesPort(listOf(cuisine, bathroom, livingRoom, toilets, trash))
        val publisher = CapturingEventsPublisher()

        val service = PlanningService(
            planningRepository = repo,
            availableResponsiblesPort = responsiblesPort,
            activeResponsibilitiesPort = responsibilitiesPort,
            rotationPolicy = rotationPolicy,
            planningFactory = DefaultPlanningFactory(),
            eventsPublisher = publisher
        )

        val initial = Planning(
            period = p0,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom, toilets, trash),
            assignments = listOf(
                Assignment(theo, bathroom),
                Assignment(theo, toilets),
                Assignment(charles, livingRoom),
                Assignment(charles, trash),
                Assignment(fabio, cuisine)
            )
        )
        repo.save(initial)

        val next = service.generatePlanning(GeneratePlanningCommand(period = p1)).planning

        val loads = next.assignments.groupBy { it.responsible }.mapValues { it.value.size }
        assertThat(loads.values.maxOrNull()!! - loads.values.minOrNull()!!).isLessThanOrEqualTo(1)
        assertThat(loads.values.sorted()).isEqualTo(listOf(1, 2, 2))

        val hadOneInPrevious = initial.assignments.groupBy { it.responsible }.mapValues { it.value.size }
            .filterValues { it == 1 }
            .keys
            .single()

        assertThat(loads.getOrDefault(hadOneInPrevious, 0)).isEqualTo(2)
        assertThat(publisher.publishedEvents.filterIsInstance<PlanningGenerated>().map { it.period })
            .containsExactly(p1)
    }

    @Test
    fun absence_de_theo_sur_deux_periodes_puis_retour() {
        val p0 = Period(LocalDate.of(2026, 1, 5))
        val p1 = Period(LocalDate.of(2026, 1, 12))
        val p2 = Period(LocalDate.of(2026, 1, 19))
        val p3 = Period(LocalDate.of(2026, 1, 26))

        val repo = InMemoryPlanningRepository()
        val responsiblesPort = PeriodAvailableResponsiblesPort(
            mapOf(
                p0 to listOf(fabio, theo, charles),
                p1 to listOf(fabio, charles),
                p2 to listOf(fabio, charles),
                p3 to listOf(fabio, theo, charles)
            )
        )
        val responsibilitiesPort = FixedActiveResponsibilitiesPort(listOf(cuisine, bathroom, livingRoom))
        val publisher = CapturingEventsPublisher()

        val service = PlanningService(
            planningRepository = repo,
            availableResponsiblesPort = responsiblesPort,
            activeResponsibilitiesPort = responsibilitiesPort,
            rotationPolicy = rotationPolicy,
            planningFactory = DefaultPlanningFactory(),
            eventsPublisher = publisher
        )

        val initial = Planning(
            period = p0,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, livingRoom),
                Assignment(theo, cuisine),
                Assignment(charles, bathroom)
            )
        )
        repo.save(initial)

        val s1 = service.generatePlanning(GeneratePlanningCommand(period = p1)).planning
        assertThat(s1.assignments.map { it.responsible }.toSet()).doesNotContain(theo)
        assertThat(s1.assignments).containsExactlyInAnyOrder(
            Assignment(charles, livingRoom),
            Assignment(charles, cuisine),
            Assignment(fabio, bathroom)
        )

        val s2 = service.generatePlanning(GeneratePlanningCommand(period = p2)).planning
        assertThat(s2.assignments.map { it.responsible }.toSet()).doesNotContain(theo)
        assertThat(s2.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, livingRoom,),
            Assignment(fabio, cuisine),
            Assignment(charles, bathroom)
        )

        val s3 = service.generatePlanning(GeneratePlanningCommand(period = p3)).planning
        assertThat(s3.assignments.map { it.responsible }.toSet()).contains(theo)
        assertThat(s3.assignments).containsExactlyInAnyOrder(
            Assignment(fabio, bathroom),
            Assignment(theo, livingRoom),
            Assignment(charles, cuisine)
        )
    }

    @Test
    fun ajout_de_responsabilite_a_S_plus_1() {
        val p0 = Period(LocalDate.of(2026, 1, 5))
        val p1 = Period(LocalDate.of(2026, 1, 12))

        val repo = InMemoryPlanningRepository()
        val responsiblesPort = FixedAvailableResponsiblesPort(listOf(fabio, theo, charles))
        val responsibilitiesPort = PeriodActiveResponsibilitiesPort(
            mapOf(
                p0 to listOf(cuisine, bathroom, livingRoom),
                p1 to listOf(cuisine, bathroom, livingRoom, toilets)
            )
        )
        val publisher = CapturingEventsPublisher()

        val service = PlanningService(
            planningRepository = repo,
            availableResponsiblesPort = responsiblesPort,
            activeResponsibilitiesPort = responsibilitiesPort,
            rotationPolicy = rotationPolicy,
            planningFactory = DefaultPlanningFactory(),
            eventsPublisher = publisher
        )

        val initial = Planning(
            period = p0,
            responsibles = listOf(fabio, theo, charles),
            responsibilities = listOf(cuisine, bathroom, livingRoom),
            assignments = listOf(
                Assignment(fabio, cuisine),
                Assignment(theo, bathroom),
                Assignment(charles, livingRoom)
            )
        )
        repo.save(initial)

        val next = service.generatePlanning(GeneratePlanningCommand(period = p1)).planning

        assertThat(next.responsibilities).containsExactlyInAnyOrder(cuisine, bathroom, livingRoom, toilets)
        assertThat(next.assignments.map { it.responsibility }.toSet())
            .isEqualTo(setOf(cuisine, bathroom, livingRoom, toilets))

        val loads = next.assignments.groupBy { it.responsible }.mapValues { it.value.size }
        assertThat(loads.values.maxOrNull()!! - loads.values.minOrNull()!!).isLessThanOrEqualTo(1)
        assertThat(loads.values.sorted()).isEqualTo(listOf(1, 1, 2))
    }


    private class FixedAvailableResponsiblesPort(
        private val responsibles: List<Responsible>
    ) : AvailableResponsiblesPort {
        override fun getFor(period: Period): List<Responsible> = responsibles
    }

    private class FixedActiveResponsibilitiesPort(
        private val responsibilities: List<Responsibility>
    ) : ActiveResponsibilitiesPort {
        override fun getFor(period: Period): List<Responsibility> = responsibilities
    }

    private class CapturingEventsPublisher : EventsPublisher {
        val publishedEvents = mutableListOf<DomainEvent>()
        override fun publish(events: List<DomainEvent>) {
            publishedEvents += events
        }
    }

    private class PeriodAvailableResponsiblesPort(
        private val perPeriod: Map<Period, List<Responsible>>
    ) : AvailableResponsiblesPort {
        override fun getFor(period: Period): List<Responsible> =
            requireNotNull(perPeriod[period]) { "No responsibles configured for period=$period" }
    }

    private class PeriodActiveResponsibilitiesPort(
        private val perPeriod: Map<Period, List<Responsibility>>
    ) : ActiveResponsibilitiesPort {
        override fun getFor(period: Period): List<Responsibility> =
            requireNotNull(perPeriod[period]) { "No responsibilities configured for period=$period" }
    }
}
