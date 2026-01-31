package cat.ohmushi.kolok.planning.adapters.out.events

import cat.ohmushi.kolok.planning.adapters.infrastructure.User
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import cat.ohmushi.kolok.planning.domain.planning.Planning
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class PlanningMessageFormatter {

    private val dateFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

    fun formatCompact(planning: Planning, absents: List<Responsible>, allUsers: List<User>): String {
        val sb = StringBuilder()

        sb.appendLine("🧹 **Planning ménage - semaine du ${planning.period.start.format(dateFormatter)}**")
        sb.appendLine()

        val grouped = planning.assignments
            .groupBy { it.responsible }
            .toSortedMap(compareBy { it.name })

        val maxNameLength = grouped.keys.maxOfOrNull { it.name.length } ?: 0

        grouped.forEach { (responsible, assignments) ->
            val snowflake = allUsers.find { it.responsible == responsible.name }?.id
            val mention = if (snowflake != null) "<@$snowflake>" else "?"
            val paddedMention = mention.padEnd(maxNameLength)
            val tasks = assignments.joinToString(", ") { it.responsibility.name }
            sb.appendLine("$paddedMention → $tasks")
        }

        if (absents.isNotEmpty()) {
            sb.appendLine()
            absents.sortedBy { it.name }.forEach { absent ->
                sb.appendLine("🚫 <@${allUsers.find { it.responsible == absent.name }?.id}> est absent cette semaine")
            }
        }

        return sb.toString().trimEnd()
    }
}
