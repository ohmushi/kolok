package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.Period

data class ResponsibilitiesChanged(val period: Period) : DomainEvent