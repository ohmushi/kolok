package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.adapters.infrastructure.User
import cat.ohmushi.kolok.planning.domain.Responsible

interface UserIdentityLinkRepository {
    fun findResponsibleIdByUserId(userId: String): Responsible?
    fun findUsersByResponsibles(responsibles: List<Responsible>): List<User>
}