package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

interface CancelAbsenceUseCase {
    fun cancelAbsence(command: CancelAbsenceCommand)
}

data class CancelAbsenceCommand(
    val responsible: Responsible,
    val from: Period,
    val periodsCount: Int = 1,
) {
    init {
        require(periodsCount >= 1) { "periodsCount must be >= 1" }
    }
}
