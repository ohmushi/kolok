package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

interface RecordAbsenceUseCase {
    fun recordAbsence(command: RecordAbsenceCommand)
}

data class RecordAbsenceCommand(
    val responsible: Responsible,
    val from: Period,
    val periodsCount: Int = 1,
) {
    init {
        require(periodsCount >= 1) { "periodsCount must be >= 1" }
    }
}
