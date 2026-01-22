package cat.ohmushi.kolok.planning.domain

sealed interface DomainException {
}

class InvalidPlanningException: DomainException {}
class InvalidRotationRequestException: DomainException {}