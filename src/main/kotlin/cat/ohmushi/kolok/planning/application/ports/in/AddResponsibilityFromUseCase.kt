package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface AddResponsibilityFromUseCase {
    fun addResponsibility(command: AddResponsibilityFromCommand)
}

data class AddResponsibilityFromCommand(
    val responsibility: Responsibility,
    val from: Period
)
