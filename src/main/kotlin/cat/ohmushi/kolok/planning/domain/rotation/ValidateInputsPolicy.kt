package cat.ohmushi.kolok.planning.domain.rotation

class ValidateInputsPolicy : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        require(request.responsibles.isNotEmpty())
        require(request.responsibilities.isNotEmpty())

        require(request.responsibles.all { it.name.isNotBlank() })
        require(request.responsibilities.all { it.name.isNotBlank() })

        require(request.responsibles.distinct().size == request.responsibles.size)
        require(request.responsibilities.distinct().size == request.responsibilities.size)

        return draft ?: RotationDraft(assignments = emptyList())
    }
}