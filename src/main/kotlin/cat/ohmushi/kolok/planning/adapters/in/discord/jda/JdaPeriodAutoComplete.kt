package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda

import cat.ohmushi.kolok.planning.domain.planning.Period
import net.dv8tion.jda.api.interactions.commands.Command
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object JdaPeriodAutoComplete {
    fun nextPeriods(
        from: LocalDate = LocalDate.now(),
        count: Int = 5,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
    ): List<Command.Choice> {
        require(count >= 0)

        val first = Period.firstAfter(from)
        return (0 until count).map {
            val value = first.plus(it.toLong()).start.format(formatter)
            Command.Choice(value, value)
        }
    }
}

