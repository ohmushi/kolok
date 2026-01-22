package cat.ohmushi.kolok.planning

import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileAvailableResponsiblesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FilePlanningRepository
import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.services.PlanningService
import cat.ohmushi.kolok.planning.bootstrap.Wiring
import cat.ohmushi.kolok.planning.domain.DefaultPlanningFactory
import cat.ohmushi.kolok.planning.domain.DomainEvent
import cat.ohmushi.kolok.planning.domain.Period
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
    val availableResponsiblesPort = FileAvailableResponsiblesPort(catalog = wiring.fileCatalog(mapper))
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
