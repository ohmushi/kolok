package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface AddResponsibilityUseCase {
    fun addResponsibility(command: AddResponsibilityCommand)
}

data class AddResponsibilityCommand(
    val responsibility: Responsibility,
    val from: Period
)
