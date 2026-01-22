package cat.ohmushi.kolok.planning.domain.rotation

class CompositeRotationPolicy(
    private val policies: List<RotationPolicy>
) : RotationPolicy {

    override fun apply(request: RotationRequest, draft: RotationDraft?): RotationDraft {
        require(policies.isNotEmpty())
        var current = draft
        for (p in policies) {
            current = p.apply(request, current)
        }
        return requireNotNull(current)
    }
}