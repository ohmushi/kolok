package cat.ohmushi.kolok.planning.adapters.out.persistence.discord

import cat.ohmushi.kolok.planning.adapters.infrastructure.User
import cat.ohmushi.kolok.planning.adapters.infrastructure.JsonPersistence
import cat.ohmushi.kolok.planning.application.ports.out.UserIdentityLinkRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible
import org.springframework.stereotype.Repository

@Repository
class FileUserIdentityLinkRepository(
     val jsonPersistence: JsonPersistence,
    val rosterProvider: RosterProvider,
) : UserIdentityLinkRepository {
    override fun findResponsibleIdByUserId(userId: String): Responsible? {
        val responsible = jsonPersistence.read().users.find { it.id == userId }
        return rosterProvider.roster().find { it.name == responsible?.responsible }
    }

    override fun findUsersByResponsibles(responsibles: List<Responsible>): List<User> {
        return jsonPersistence.read().users.filter { it -> it.responsible in responsibles.map { it.name } }
    }
}