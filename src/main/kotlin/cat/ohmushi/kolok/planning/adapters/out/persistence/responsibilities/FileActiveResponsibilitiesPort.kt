package cat.ohmushi.kolok.planning.adapters.out.persistence.responsibilities

import cat.ohmushi.kolok.planning.adapters.out.persistence.json.FileCatalog
import cat.ohmushi.kolok.planning.application.ports.out.ActiveResponsibilitiesPort
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility

class FileActiveResponsibilitiesPort(
    private val catalog: FileCatalog
) : ActiveResponsibilitiesPort {

    override fun getFor(period: Period): List<Responsibility> {
        val cfg = catalog.read()
        val names = catalog.resolveByPeriod(period.start, cfg.responsibilitiesByPeriod)
        require(names.isNotEmpty()) { "No responsibilities configured for period=${period.start}, check='${catalog.path.toAbsolutePath()}'" }
        return names.map { Responsibility(it) }
    }
}