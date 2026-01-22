package cat.ohmushi.kolok.planning.bootstrap

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

@Configuration
class Wiring {

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


}