package cat.ohmushi.kolok.planning.domain.responsibilities

import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.responsibilities.Responsibility
import cat.ohmushi.kolok.planning.domain.events.DomainEvent
import cat.ohmushi.kolok.planning.domain.events.ResponsibilitiesDefined
import cat.ohmushi.kolok.planning.domain.events.ResponsibilityAddedFrom
import cat.ohmushi.kolok.planning.domain.events.ResponsibilityRemovedFrom

class ResponsibilitiesCatalog private constructor(
    private val versions: List<ResponsibilitiesVersion>,
    private val pendingEvents: List<DomainEvent>
) {

    companion object {
        fun create(initialFrom: Period, responsibilities: Set<Responsibility>): ResponsibilitiesCatalog {
            val v = ResponsibilitiesVersion(from = initialFrom, responsibilities = responsibilities)
            return ResponsibilitiesCatalog(versions = listOf(v), pendingEvents = emptyList())
        }
    }

    fun activeFor(period: Period): List<Responsibility> {
        val version = versions
            .filter { !it.from.start.isAfter(period.start) }
            .maxByOrNull { it.from.start }
            ?: return emptyList()

        return version.responsibilities.sortedBy { it.name }
    }

    fun addFrom(from: Period, responsibility: Responsibility): ResponsibilitiesCatalog {
        require(responsibility.name.isNotBlank())

        val base = activeFor(from).toSet()
        if (responsibility in base) return this

        val nextSet = base + responsibility
        val nextVersions = upsertVersion(from, nextSet)
        val event = ResponsibilityAddedFrom(from = from, responsibility = responsibility)

        return ResponsibilitiesCatalog(nextVersions, pendingEvents + event)
    }

    fun removeFrom(from: Period, responsibility: Responsibility): ResponsibilitiesCatalog {
        require(responsibility.name.isNotBlank())

        val base = activeFor(from).toSet()
        require(responsibility in base)

        val nextSet = base - responsibility
        require(nextSet.isNotEmpty())

        val nextVersions = upsertVersion(from, nextSet)
        val event = ResponsibilityRemovedFrom(from = from, responsibility = responsibility)

        return ResponsibilitiesCatalog(nextVersions, pendingEvents + event)
    }

    fun consumeEvents(): Pair<ResponsibilitiesCatalog, List<DomainEvent>> =
        ResponsibilitiesCatalog(
            versions = versions,
            pendingEvents = emptyList()
        ) to pendingEvents

    fun snapshotVersions(): List<ResponsibilitiesVersion> =
        versions.toList()

    private fun upsertVersion(from: Period, responsibilities: Set<Responsibility>): List<ResponsibilitiesVersion> {
        val v = ResponsibilitiesVersion(from, responsibilities)

        val kept = versions.filterNot { it.from == from }
        val next = (kept + v).sortedBy { it.from.start }

        return next
    }

    fun defineFor(from: Period, responsibilities: Set<Responsibility>): ResponsibilitiesCatalog {
        val version = ResponsibilitiesVersion(from = from, responsibilities = responsibilities)

        val kept = versions.filterNot { it.from == from }
        val nextVersions = (kept + version).sortedBy { it.from.start }

        val event = ResponsibilitiesDefined(from = from, responsibilities = responsibilities)

        return ResponsibilitiesCatalog(
            versions = nextVersions,
            pendingEvents = pendingEvents + event
        )
    }
}