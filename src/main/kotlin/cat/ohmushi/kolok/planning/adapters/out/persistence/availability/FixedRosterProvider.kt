package cat.ohmushi.kolok.planning.adapters.out.persistence.availability

import cat.ohmushi.kolok.planning.application.ports.out.RosterProvider
import cat.ohmushi.kolok.planning.domain.Responsible
import org.springframework.stereotype.Repository

@Repository
class FixedRosterProvider : RosterProvider {
    override fun roster(): Set<Responsible> =
        setOf(
            Responsible("Fabio"),
            Responsible("Charles"),
            Responsible("Theo"),
        )
}