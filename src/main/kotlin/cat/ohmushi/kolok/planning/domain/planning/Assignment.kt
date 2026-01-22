package cat.ohmushi.kolok.planning.domain.planning

import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible

data class Assignment(val responsible: Responsible, val responsibility: Responsibility) {
    override fun toString(): String {
        return "${responsible.name} → ${responsibility.name}"
    }
}