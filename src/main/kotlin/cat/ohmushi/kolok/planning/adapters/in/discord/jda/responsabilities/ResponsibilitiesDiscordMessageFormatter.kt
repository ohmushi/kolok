package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import net.dv8tion.jda.api.entities.Message.MAX_CONTENT_LENGTH

internal object ResponsibilitiesDiscordMessageFormatter {

    fun format(period: Period, responsibilities: List<Responsibility>, nextPeriod: Period? = null): String {
        if (responsibilities.isEmpty()) {
            return "Aucune responsabilité active pour la période du ${period.start}."
        }

        val header = when (nextPeriod) {
            null -> "Responsabilités depuis le ${period.start} :\n"
            else -> {
                val endDate = nextPeriod.start.minusDays(1)
                "Responsabilités du ${period.start} au $endDate :\n"
            }
        }

        val body = buildString {
            responsibilities.forEach { responsibility ->
                append("- ")
                append(responsibility.name)
                append('\n')
            }
        }

        val full = (header + body).trimEnd()
        if (full.length <= MAX_CONTENT_LENGTH) {
            return full
        }

        val suffix = "\n..."
        val maxLen = (MAX_CONTENT_LENGTH - suffix.length).coerceAtLeast(0)
        return (full.take(maxLen).trimEnd() + suffix)
    }
}

