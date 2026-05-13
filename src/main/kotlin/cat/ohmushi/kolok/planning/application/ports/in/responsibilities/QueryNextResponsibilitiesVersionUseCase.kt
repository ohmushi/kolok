package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.planning.Period

interface QueryNextResponsibilitiesVersionUseCase {
    fun nextVersionAfter(period: Period): Period?
}

