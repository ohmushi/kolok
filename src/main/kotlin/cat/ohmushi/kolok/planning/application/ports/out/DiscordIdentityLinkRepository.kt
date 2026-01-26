package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.adapters.infrastructure.DiscordUser
import cat.ohmushi.kolok.planning.domain.Responsible

interface DiscordIdentityLinkRepository {
    fun findResponsibleIdByDiscordUserId(discordUserId: String): Responsible?
    fun findDiscordSnowflakesByResponsibles(responsibles: List<Responsible>): List<DiscordUser>?
}