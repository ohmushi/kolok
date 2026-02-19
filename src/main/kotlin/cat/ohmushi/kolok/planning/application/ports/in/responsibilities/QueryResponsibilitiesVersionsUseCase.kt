package cat.ohmushi.kolok.planning.application.ports.`in`.responsibilities

import cat.ohmushi.kolok.planning.domain.responsibilities.ResponsibilitiesVersion

interface QueryResponsibilitiesVersionsUseCase {
    fun snapshotResponsibilitiesVersions(): List<ResponsibilitiesVersion>
}

