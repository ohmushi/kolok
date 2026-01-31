package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.responsibilities.Responsible

interface RosterProvider {
    fun roster(): Set<Responsible>
}