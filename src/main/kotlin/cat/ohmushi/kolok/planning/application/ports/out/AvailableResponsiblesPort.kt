package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

interface AvailableResponsiblesPort {
    fun getFor(period: Period): List<Responsible>
}