package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.planning.Planning
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class PlanningMessageFormatter {

    private val dateFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

    fun formatCompact(planning: Planning, absents: List<Responsible>): String {
        val sb = StringBuilder()

        sb.appendLine("🧹 **Planning ménage — semaine du ${planning.period.start.format(dateFormatter)}**")
        sb.appendLine()

        val grouped = planning.assignments
            .groupBy { it.responsible }
            .toSortedMap(compareBy { it.name })

        val maxNameLength = grouped.keys.maxOfOrNull { it.name.length } ?: 0

        grouped.forEach { (responsible, assignments) ->
            val paddedName = responsible.name.padEnd(maxNameLength)
            val tasks = assignments.joinToString(", ") { it.responsibility.name }
            sb.appendLine("$paddedName → $tasks")
        }

        if (absents.isNotEmpty()) {
            sb.appendLine()
            absents.sortedBy { it.name }.forEach { absent ->
                sb.appendLine("🚫 ${absent.name} est absent cette semaine")
            }
        }

        return sb.toString().trimEnd()
    }
}
