package cat.ohmushi.kolok.planning.application.services

import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.AddResponsibilityCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.RemoveResponsibilityCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities.ResponsibilitiesForPeriodQuery
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.ResponsibilitiesCatalog
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ResponsibilitiesServiceTest {

    private val p1 = Period(LocalDate.of(2026, 2, 16))
    private val p2 = p1.next()

    private val a = Responsibility("A")
    private val b = Responsibility("B")

    @Test
    fun `responsibilitiesFor - retourne les responsibilities actives pour la periode`() {
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))
            .addFrom(from = p2, responsibility = b)
        val repo = InMemoryResponsibilitiesCatalogRepository(catalog)
        val service = ResponsibilitiesService(repository = repo)

        assertThat(service.responsibilitiesFor(ResponsibilitiesForPeriodQuery(period = p1))).containsExactly(a)
        assertThat(service.responsibilitiesFor(ResponsibilitiesForPeriodQuery(period = p2))).containsExactly(a, b)
    }

    @Test
    fun `addResponsibility - ajoute une responsibility et persiste`() {
        val initial = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))
        val repo = InMemoryResponsibilitiesCatalogRepository(initial)
        val service = ResponsibilitiesService(repository = repo)

        service.addResponsibility(AddResponsibilityCommand(responsibility = b, from = p2))

        assertThat(repo.saved).hasSize(1)
        val saved = repo.saved.single()
        assertThat(saved.activeFor(p1)).containsExactly(a)
        assertThat(saved.activeFor(p2)).containsExactly(a, b)
    }

    @Test
    fun `addResponsibility - idempotent et ne sauvegarde pas si deja presente`() {
        val initial = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a, b))
        val repo = ForbidSaveResponsibilitiesCatalogRepository(initial)
        val service = ResponsibilitiesService(repository = repo)

        service.addResponsibility(AddResponsibilityCommand(responsibility = b, from = p2))

        assertThat(repo.saveCalls).isZero()
    }

    @Test
    fun `removeResponsibility - retire une responsibility et persiste`() {
        val initial = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a, b))
        val repo = InMemoryResponsibilitiesCatalogRepository(initial)
        val service = ResponsibilitiesService(repository = repo)

        service.removeResponsibility(RemoveResponsibilityCommand(responsibility = b, from = p2))

        assertThat(repo.saved).hasSize(1)
        val saved = repo.saved.single()
        assertThat(saved.activeFor(p1)).containsExactly(a, b)
        assertThat(saved.activeFor(p2)).containsExactly(a)
    }

    @Test
    fun `removeResponsibility - echoue si la responsibility n'est pas active et ne persiste pas`() {
        val initial = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))
        val repo = ForbidSaveResponsibilitiesCatalogRepository(initial)
        val service = ResponsibilitiesService(repository = repo)

        assertThatThrownBy {
            service.removeResponsibility(RemoveResponsibilityCommand(responsibility = b, from = p2))
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThat(repo.saveCalls).isZero()
    }

    @Test
    fun `nextVersionAfter - retourne la period de la prochaine version si elle existe`() {
        val p3 = p2.next()
        val catalog = ResponsibilitiesCatalog.create(initialFrom = p1, responsibilities = setOf(a))
            .defineFor(from = p2, responsibilities = setOf(a, b))
            .defineFor(from = p3, responsibilities = setOf(b))
        val repo = InMemoryResponsibilitiesCatalogRepository(catalog)
        val service = ResponsibilitiesService(repository = repo)

        assertThat(service.nextVersionAfter(p1)).isEqualTo(p2)
        assertThat(service.nextVersionAfter(p2)).isEqualTo(p3)
        assertThat(service.nextVersionAfter(p3)).isNull()
    }

    private class InMemoryResponsibilitiesCatalogRepository(
        private var catalog: ResponsibilitiesCatalog?,
    ) : ResponsibilitiesCatalogRepository {
        val saved = mutableListOf<ResponsibilitiesCatalog>()

        override fun get(): ResponsibilitiesCatalog? = catalog

        override fun save(catalog: ResponsibilitiesCatalog) {
            saved += catalog
            this.catalog = catalog
        }
    }

    private class ForbidSaveResponsibilitiesCatalogRepository(
        private val catalog: ResponsibilitiesCatalog,
    ) : ResponsibilitiesCatalogRepository {
        var saveCalls: Int = 0
            private set

        override fun get(): ResponsibilitiesCatalog = catalog

        override fun save(catalog: ResponsibilitiesCatalog) {
            saveCalls++
            throw AssertionError("save() ne doit pas etre appele dans ce scenario idempotent")
        }
    }
}
