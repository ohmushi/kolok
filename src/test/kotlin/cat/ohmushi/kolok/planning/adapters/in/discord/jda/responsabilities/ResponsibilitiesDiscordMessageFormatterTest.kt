package cat.ohmushi.kolok.planning.adapters.`in`.discord.jda.responsabilities

import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ResponsibilitiesDiscordMessageFormatterTest {

    private val p1 = Period(LocalDate.of(2026, 2, 16))
    private val p2 = p1.next()

    private val a = Responsibility("Alice")
    private val b = Responsibility("Bob")

    @Test
    fun `format - liste vide retourne un message specifique`() {
        val result = ResponsibilitiesDiscordMessageFormatter.format(p1, emptyList(), nextPeriod = null)

        assertThat(result).isEqualTo("Aucune responsabilité active pour la période du 2026-02-16.")
    }

    @Test
    fun `format - sans prochaine periode affiche "depuis" le debut`() {
        val result = ResponsibilitiesDiscordMessageFormatter.format(p1, listOf(a, b), nextPeriod = null)

        assertThat(result).startsWith("Responsabilités depuis le 2026-02-16 :\n- Alice\n- Bob")
    }

    @Test
    fun `format - avec prochaine periode affiche la plage du debut au avant fin`() {
        val result = ResponsibilitiesDiscordMessageFormatter.format(p1, listOf(a, b), nextPeriod = p2)

        assertThat(result).startsWith("Responsabilités du 2026-02-16 au 2026-02-22 :\n- Alice\n- Bob")
    }

    @Test
    fun `format - tronque si le message depasse MAX_CONTENT_LENGTH`() {
        val longResponsibilities = (1..500).map { Responsibility("Resp_$it") }
        val result = ResponsibilitiesDiscordMessageFormatter.format(p1, longResponsibilities, nextPeriod = null)

        assertThat(result).endsWith("...")
        assertThat(result.length).isLessThanOrEqualTo(2000) // MAX_CONTENT_LENGTH JDA
    }
}


