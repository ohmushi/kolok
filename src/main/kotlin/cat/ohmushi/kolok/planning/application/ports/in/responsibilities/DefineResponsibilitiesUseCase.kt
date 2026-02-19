package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface DefineResponsibilitiesUseCase {
    fun defineResponsibilities(command: DefineResponsibilitiesCommand)
}

data class DefineResponsibilitiesCommand(
    val from: Period,
    val responsibilities: Set<Responsibility>,
)

