package cat.ohmushi.kolok.planning.adapters.infrastructure

/**
 * Représente un identifiant utilisateur Discord (Snowflake) normalisé.
 *
 * Accepte en entrée :
 * - un snowflake brut: "1234567890"
 * - une mention: "<@1234567890>" ou "<@!1234567890>"
 *
 * Rejette : vide, ou formats non numériques.
 */
@JvmInline
value class DiscordId private constructor(val snowflake: String) {
    init {
        require(snowflake.isNotBlank()) { "DiscordId cannot be blank" }
        require(snowflake.all { it.isDigit() }) { "DiscordId must be numeric" }
    }

    override fun toString(): String = snowflake

    companion object {
        private val mentionRegex = Regex("^<@!?([0-9]+)>$")

        fun parse(raw: String): DiscordId {
            val trimmed = raw.trim()
            require(trimmed.isNotBlank()) { "Discord id is blank" }

            val match = mentionRegex.matchEntire(trimmed)
            val numeric = when {
                match != null -> match.groupValues[1]
                trimmed.all { it.isDigit() } -> trimmed
                else -> throw IllegalArgumentException("Invalid Discord id format: '$raw'")
            }

            return DiscordId(numeric)
        }
    }
}
