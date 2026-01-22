package cat.ohmushi.kolok.planning.adapters.out.persistence.json

import cat.ohmushi.kolok.planning.application.ports.out.AvailableResponsiblesPort
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsible
import org.springframework.stereotype.Repository

@Repository
class FileAvailableResponsiblesPort(
    private val catalog: FileCatalog
) : AvailableResponsiblesPort {

    override fun getFor(period: Period): List<Responsible> {
        val cfg = catalog.read()
        val names = catalog.resolveByPeriod(period.start, cfg.responsiblesByPeriod)
        require(names.isNotEmpty()) { "No responsibles configured for period=${period.start}, check='${catalog.path.toAbsolutePath()}'" }
        return names.map { Responsible(it) }
    }
}
