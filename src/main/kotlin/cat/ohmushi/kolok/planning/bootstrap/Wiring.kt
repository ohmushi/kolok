package cat.ohmushi.kolok.planning.bootstrap

import cat.ohmushi.kolok.planning.adapters.out.events.DiscordEventPublisher
import cat.ohmushi.kolok.planning.adapters.out.persistence.availability.AvailableResponsiblesAdapter
import cat.ohmushi.kolok.planning.adapters.out.persistence.availability.FixedRosterProvider
import cat.ohmushi.kolok.planning.adapters.out.persistence.availability.JsonAvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.adapters.out.persistence.JsonPersistence
import cat.ohmushi.kolok.planning.adapters.out.persistence.planning.FilePlanningRepository
import cat.ohmushi.kolok.planning.adapters.out.persistence.responsibilities.ActiveResponsibilitiesAdapter
import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.application.ports.out.AvailabilityCalendarRepository
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.application.ports.out.EventPublisher
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.planning.DefaultPlanningFactory
import cat.ohmushi.kolok.planning.domain.planning.PlanningFactory
import cat.ohmushi.kolok.planning.domain.rotation.BalanceLoadPolicy
import cat.ohmushi.kolok.planning.domain.rotation.BootstrapIfNoPreviousPolicy
import cat.ohmushi.kolok.planning.domain.rotation.CompositeRotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.CoverAllResponsibilitiesPolicy
import cat.ohmushi.kolok.planning.domain.rotation.FairnessGuardPolicy
import cat.ohmushi.kolok.planning.domain.rotation.ProjectPreviousAssignmentsPolicy
import cat.ohmushi.kolok.planning.domain.rotation.RotationPolicy
import cat.ohmushi.kolok.planning.domain.rotation.ValidateInputsPolicy
import cat.ohmushi.kolok.planning.domain.rotation.ShiftByOneRotationPolicy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.file.Paths

@Configuration
class Wiring {

    val catalogPath = Paths.get("data/catalog.json")
    val planningsPath = Paths.get("data/plannings.json")


    @Bean
    fun rotationPolity(): RotationPolicy = CompositeRotationPolicy(
        listOf(
            ValidateInputsPolicy(),
            BootstrapIfNoPreviousPolicy(),
            ProjectPreviousAssignmentsPolicy(),
            ShiftByOneRotationPolicy(),
            CoverAllResponsibilitiesPolicy(),
            BalanceLoadPolicy(),
            FairnessGuardPolicy(),
        )
    )

    @Bean
    fun jsonMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }

    @Bean
    fun fileCatalog(mapper: ObjectMapper): JsonPersistence {

        return JsonPersistence(
            path = catalogPath,
            mapper = mapper,
        )
    }

    @Bean
    fun activeResponsibilitiesPort(responsabilityCatalogRepository: ResponsibilitiesCatalogRepository): ActiveResponsibilitiesPort =
        ActiveResponsibilitiesAdapter(
            repository = responsabilityCatalogRepository
        )

    @Bean
    fun availableResponsiblesPort(availabilityCalendarRepository: AvailabilityCalendarRepository, rosterProvider: FixedRosterProvider): AvailableResponsiblesPort =
        AvailableResponsiblesAdapter(
            repository = availabilityCalendarRepository,
            rosterProvider = rosterProvider,
        )

    @Bean
    fun rosterProvider(): RosterProvider = FixedRosterProvider()

    @Bean
    fun planningRepository(mapper: ObjectMapper): PlanningRepository = FilePlanningRepository(
        path = planningsPath,
        mapper = mapper
    )

    @Bean
    fun eventPublisher(): EventPublisher = object : EventPublisher {
        override fun publish(events: List<DomainEvent>) {
            // DO nothing
        }
    }
}