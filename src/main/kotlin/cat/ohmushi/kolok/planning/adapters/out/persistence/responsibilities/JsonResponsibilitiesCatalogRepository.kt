package cat.ohmushi.kolok.planning.adapters.out.persistence.responsibilities

import cat.ohmushi.kolok.planning.adapters.infrastructure.JsonPersistence
import cat.ohmushi.kolok.planning.adapters.infrastructure.ResponsibilitiesVersionFileEntry
import cat.ohmushi.kolok.planning.application.ports.out.ResponsibilitiesCatalogRepository
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.responsibilities.ResponsibilitiesCatalog
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JsonResponsibilitiesCatalogRepository(
    private val jsonPersistence: JsonPersistence
) : ResponsibilitiesCatalogRepository {

    override fun get(): ResponsibilitiesCatalog? {
        val file = jsonPersistence.read()
        if (file.responsibilityVersions.isEmpty()) return null

        val versions = file.responsibilityVersions
            .map {
                Period(LocalDate.parse(it.from)) to it.responsibilities.map(::Responsibility).toSet()
            }
            .sortedBy { it.first.start }

        val (firstFrom, firstSet) = versions.first()
        var responsibilities = ResponsibilitiesCatalog.create(firstFrom, firstSet)

        for ((from, set) in versions.drop(1)) {
            responsibilities = responsibilities.defineFor(from, set)
        }

        val (clean, _) = responsibilities.consumeEvents()
        return clean
    }

    override fun save(catalog: ResponsibilitiesCatalog) {
        val file = jsonPersistence.read()

        val versions = catalog.snapshotVersions().map { v ->
            ResponsibilitiesVersionFileEntry(
                from = v.from.start.toString(),
                responsibilities = v.responsibilities.map { it.name }.sorted()
            )
        }.sortedBy { LocalDate.parse(it.from) }

        jsonPersistence.write(file.copy(responsibilityVersions = versions))
    }
}