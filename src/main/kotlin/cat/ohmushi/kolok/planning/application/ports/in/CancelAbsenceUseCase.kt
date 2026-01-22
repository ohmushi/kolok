package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible

interface CancelAbsenceUseCase {
    fun cancelAbsence(command: CancelAbsenceCommand)
}

data class CancelAbsenceCommand(
    val responsible: Responsible,
    val from: Period,
    val to: Period
)