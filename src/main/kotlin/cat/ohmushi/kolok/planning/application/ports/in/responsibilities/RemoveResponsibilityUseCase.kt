package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility

interface RemoveResponsibilityUseCase {
    fun removeResponsibility(command: RemoveResponsibilityCommand)
}

data class RemoveResponsibilityCommand(
    val responsibility: Responsibility,
    val from: Period
)
