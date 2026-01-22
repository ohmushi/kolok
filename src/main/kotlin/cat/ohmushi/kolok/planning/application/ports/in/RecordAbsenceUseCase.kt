package cat.ohmushi.kolok.planning.application.ports.`in`

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible

interface RecordAbsenceUseCase {
    fun recordAbsence(command: RecordAbsenceCommand)
}

data class RecordAbsenceCommand(
    val responsible: Responsible,
    val from: Period,
    val to: Period
)