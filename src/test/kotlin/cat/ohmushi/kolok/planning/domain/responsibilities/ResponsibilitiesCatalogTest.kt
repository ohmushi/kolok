package cat.ohmushi.kolok.planning.domain.responsibilities

import cat.ohmushi.kolok.planning.domain.events.ResponsibilitiesDefined
import cat.ohmushi.kolok.planning.domain.events.ResponsibilityAddedFrom
import cat.ohmushi.kolok.planning.domain.events.ResponsibilityRemovedFrom
import cat.ohmushi.kolok.planning.domain.planning.Period
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ResponsibilitiesCatalogTest {

    private val p1 = Period(LocalDate.of(2026, 2, 16))
    private val p2 = p1.next()
    private val p3 = p2.next()

    private val a = Responsibility("A")
    private val b = Responsibility("B")
    private val c = Responsibility("C")

    @Test
    fun `activeFor - retourne vide si aucune version ne s'applique`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p2, responsibilities = setOf(a))

        assertThat(catalog.activeFor(p1)).isEmpty()
    }

    @Test
    fun `activeFor - retourne la version active la plus recente (from le plus grand) et trie par nom`() {
        val catalog = ResponsibilitiesCatalog
            .create(initialFrom = p1, responsibilities = setOf(b, a))
            .defineFor(from = p3, responsibilities = setOf(c))

        assertThat(catalog.activeFor(p1)).containsExactly(a, b)
        assertThat(catalog.activeFor(p2)).containsExactly(a, b)
        assertThat(catalog.activeFor(p3)).containsExactly(c)
    }

    @Test
    fun `addFrom - idempotent si la responsibility est deja presente`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a, b))

        val updated = catalog.addFrom(from = p2, responsibility = a)

        assertThat(updated).isSameAs(catalog)
    }

    @Test
    fun `addFrom - ajoute une responsibility a partir d'une periode et emet un event`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))

        val updated = catalog.addFrom(from = p2, responsibility = b)

        assertThat(updated.activeFor(p1)).containsExactly(a)
        assertThat(updated.activeFor(p2)).containsExactly(a, b)
        assertThat(updated.activeFor(p3)).containsExactly(a, b)

        val (afterConsume, events) = updated.consumeEvents()
        assertThat(events).containsExactly(ResponsibilityAddedFrom(from = p2, responsibility = b))
        assertThat(afterConsume.consumeEvents().second).isEmpty()
    }

    @Test
    fun `removeFrom - refuse de retirer une responsibility absente`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))

        assertThatThrownBy { catalog.removeFrom(from = p2, responsibility = b) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `removeFrom - refuse de rendre l'ensemble vide`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))

        assertThatThrownBy { catalog.removeFrom(from = p2, responsibility = a) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `removeFrom - retire une responsibility a partir d'une periode et emet un event`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a, b))

        val updated = catalog.removeFrom(from = p2, responsibility = b)

        assertThat(updated.activeFor(p1)).containsExactly(a, b)
        assertThat(updated.activeFor(p2)).containsExactly(a)
        assertThat(updated.activeFor(p3)).containsExactly(a)

        val (_, events) = updated.consumeEvents()
        assertThat(events).containsExactly(ResponsibilityRemovedFrom(from = p2, responsibility = b))
    }

    @Test
    fun `defineFor - definit une version exacte (upsert) et emet un event a chaque fois`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))

        val updated = catalog
            .defineFor(from = p2, responsibilities = setOf(a, b))
            .defineFor(from = p2, responsibilities = setOf(a, c))

        assertThat(updated.activeFor(p1)).containsExactly(a)
        assertThat(updated.activeFor(p2)).containsExactly(a, c)
        assertThat(updated.snapshotVersions().map { it.from }).containsExactly(p1, p2)

        val (_, events) = updated.consumeEvents()
        assertThat(events).containsExactly(
            ResponsibilitiesDefined(from = p2, responsibilities = setOf(a, b)),
            ResponsibilitiesDefined(from = p2, responsibilities = setOf(a, c)),
        )
    }

    @Test
    fun `snapshotVersions - renvoie les versions ordonnees par from croissant`() {
        val catalog = ResponsibilitiesCatalog
            .create(initialFrom = p2, responsibilities = setOf(a))
            .defineFor(from = p1, responsibilities = setOf(a, b))
            .defineFor(from = p3, responsibilities = setOf(c))

        assertThat(catalog.snapshotVersions().map { it.from }).containsExactly(p1, p2, p3)
    }

    @Test
    fun `nextVersionAfter - retourne null si aucune version apres la periode donnee`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))

        assertThat(catalog.nextVersionAfter(p1)).isNull()
        assertThat(catalog.nextVersionAfter(p2)).isNull()
    }

    @Test
    fun `nextVersionAfter - retourne la periode de la prochaine version`() {
        val catalog = ResponsibilitiesCatalog
            .create(initialFrom = p1, responsibilities = setOf(a))
            .defineFor(from = p2, responsibilities = setOf(a, b))
            .defineFor(from = p3, responsibilities = setOf(c))

        assertThat(catalog.nextVersionAfter(p1)).isEqualTo(p2)
        assertThat(catalog.nextVersionAfter(p2)).isEqualTo(p3)
        assertThat(catalog.nextVersionAfter(p3)).isNull()
    }
}