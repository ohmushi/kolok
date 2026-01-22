package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility

interface RemoveResponsibilityFromUseCase {
    fun removeResponsibility(command: RemoveResponsibilityFromCommand)
}

data class RemoveResponsibilityFromCommand(
    val responsibility: Responsibility,
    val from: Period
)
