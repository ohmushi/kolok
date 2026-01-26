package cat.ohmushi.kolok.planning.adapters.out.persistence.discord

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordUser
import cat.ohmushi.kolok.planning.adapters.infrastructure.JsonPersistence
import cat.ohmushi.kolok.planning.application.ports.out.DiscordIdentityLinkRepository
import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Responsible
import org.springframework.stereotype.Repository

@Repository
class FileDiscordIdentityLinkRepository(
     val jsonPersistence: JsonPersistence,
    val rosterProvider: RosterProvider,
) : DiscordIdentityLinkRepository {
    override fun findResponsibleIdByDiscordUserId(discordUserId: String): Responsible? {
        val responsible = jsonPersistence.read().discordUsers.find { it.snowflake == discordUserId }
        return rosterProvider.roster().find { it.name == responsible?.responsible }
    }

    override fun findDiscordSnowflakesByResponsibles(responsibles: List<Responsible>): List<DiscordUser>? {
        return jsonPersistence.read().discordUsers.filter { it -> it.responsible in responsibles.map { it.name } }
    }
}