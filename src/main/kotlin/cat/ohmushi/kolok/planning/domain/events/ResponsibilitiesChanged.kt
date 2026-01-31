package cat.ohmushi.kolok.planning.domain.events

import cat.ohmushi.kolok.planning.domain.planning.Period

data class ResponsibilitiesChanged(val period: Period) : DomainEvent