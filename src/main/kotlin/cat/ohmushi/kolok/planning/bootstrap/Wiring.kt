package cat.ohmushi.kolok.planning.bootstrap

import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileAvailableResponsiblesPort
import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileCatalog
import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FilePlanningRepository
import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
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
    fun fileCatalog(mapper: ObjectMapper): FileCatalog {

        return FileCatalog(
            path = catalogPath,
            mapper = mapper,
        )
    }

    @Bean
    fun activeResponsibilitiesPort(catalog: FileCatalog): ActiveResponsibilitiesPort = FileActiveResponsibilitiesPort(catalog)

    @Bean
    fun availableResponsiblesPort(catalog: FileCatalog): AvailableResponsiblesPort = FileAvailableResponsiblesPort(catalog)

    @Bean
    fun planningRepository(mapper: ObjectMapper): PlanningRepository = FilePlanningRepository(
        path = planningsPath,
        mapper = mapper
    )
}