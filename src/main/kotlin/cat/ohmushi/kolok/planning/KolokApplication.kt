package cat.ohmushi.kolok.planning

import cat.ohmushi.kolok.planning.adapters.out.persistence.availability.AvailabilityCalendarAvailableResponsiblesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.responsibilities.FileActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.planning.FilePlanningRepository
import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.application.services.PlanningService
import cat.ohmushi.kolok.planning.bootstrap.Wiring
import cat.ohmushi.kolok.planning.domain.planning.DefaultPlanningFactory
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import java.time.LocalDate
import java.time.Month

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan(
    basePackages = ["domain", "application"],
    includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [ApplicationComponent::class])]
)
class KolokApplication

fun main(args: Array<String>) {
    runApplication<KolokApplication>(*args)
    
    val wiring = Wiring()
    val mapper = wiring.jsonMapper();
    val planningRepository = FilePlanningRepository(path = wiring.planningsPath, mapper = mapper)
    val rotationPolicy: RotationPolicy = wiring.rotationPolity()
    val planningFactory = DefaultPlanningFactory()
    val eventPublisher: EventPublisher = object : EventPublisher {
        override fun publish(events: List<DomainEvent>) {
            println(events)
        }
    }

    val fileCatalog = wiring.fileCatalog(mapper)
    val roster = object : RosterProvider {
        override fun roster(): Set<Responsible> {
            return setOf(Responsible("theo"), Responsible("fabio"), Responsible("charles"))
        }

    }
    val availableResponsiblesPort = AvailabilityCalendarAvailableResponsiblesPort(repository = wiring.availabilityCalendarRepository(fileCatalog, roster))
    val activeResponsibilitiesPort = FileActiveResponsibilitiesPort(catalog = wiring.fileCatalog(mapper))
    
    val generatePlanningUseCase = PlanningService(
        planningRepository = planningRepository,
        rotationPolicy = rotationPolicy,
        planningFactory = planningFactory,
        eventPublisher = eventPublisher,
        availableResponsiblesPort = availableResponsiblesPort,
        activeResponsibilitiesPort = activeResponsibilitiesPort
    )
    
    val res = generatePlanningUseCase.generatePlanning(GeneratePlanningCommand(
        period = Period(start = LocalDate.of(2026, Month.JANUARY, 12))
    ))
    println(res)
}
