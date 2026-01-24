package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.responsibilities.ResponsibilitiesCatalog

interface ResponsibilitiesCatalogRepository {
    fun get(): ResponsibilitiesCatalog?
    fun save(catalog: ResponsibilitiesCatalog)
}
