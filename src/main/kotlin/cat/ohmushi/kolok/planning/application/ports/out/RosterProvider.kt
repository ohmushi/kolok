package cat.ohmushi.kolok.planning.application.ports.out

import cat.ohmushi.kolok.planning.domain.Responsible

interface RosterProvider {
    fun roster(): Set<Responsible>
}