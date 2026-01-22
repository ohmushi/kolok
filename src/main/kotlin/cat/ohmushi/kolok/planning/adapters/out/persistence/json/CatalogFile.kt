data class CatalogFile(
    val responsiblesByPeriod: Map<String, List<String>> = emptyMap(),
    val responsibilitiesByPeriod: Map<String, List<String>> = emptyMap()
)

data class PlanningFileEntry(
    val periodStart: String,
    val responsibles: List<String>,
    val responsibilities: List<String>,
    val assignments: List<AssignmentFileEntry>
)

data class AssignmentFileEntry(
    val responsible: String,
    val responsibility: String
)
