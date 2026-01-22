package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility

interface ActiveResponsibilitiesPort {
    fun getFor(period: Period): List<Responsibility>
}