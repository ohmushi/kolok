package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class JdaResponsibilitiesForPeriodDiscordCommandTest {

    private val p1 = Period(LocalDate.of(2026, 2, 16))
    private val p2 = p1.next()

    @Test
    fun `message avec prochaine periode affiche la plage du debut au jour avant`() {
        val responsibilities = listOf(
            Responsibility("Alice"),
            Responsibility("Bob"),
        )

        val message = ResponsibilitiesDiscordMessageFormatter.format(p1, responsibilities, p2)

        assertThat(message).contains("du 2026-02-16 au 2026-02-22")
        assertThat(message).contains("Alice")
        assertThat(message).contains("Bob")
    }

    @Test
    fun `message sans prochaine periode affiche depuis le debut`() {
        val responsibilities = listOf(
            Responsibility("Charlie"),
        )

        val message = ResponsibilitiesDiscordMessageFormatter.format(p1, responsibilities, null)

        assertThat(message).contains("depuis le 2026-02-16")
        assertThat(message).contains("Charlie")
        assertThat(message).doesNotContain(" au ")
    }
}



