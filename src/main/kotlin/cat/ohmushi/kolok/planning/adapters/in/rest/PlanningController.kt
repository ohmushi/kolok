package cat.ohmushi.kolok.planning.adapters.`in`.rest

import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.domain.planning.Period
import cat.ohmushi.kolok.planning.domain.planning.Planning
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("planning")
class PlanningRestController(
    private val getPlanningUseCase: GeneratePlanningUseCase
) {
    @GetMapping("/{period}")
    fun getByPeriod(@PathVariable period: String): ResponseEntity<PlanningResponse> {
        val start = runCatching { LocalDate.parse(period) }.getOrNull()
            ?: return ResponseEntity.badRequest().build()

        val result = getPlanningUseCase.generatePlanning(
            GeneratePlanningCommand(period = Period(start))
        )

        return ResponseEntity.ok(result.planning.toResponse())
    }
}

data class PlanningResponse(
    val periodStart: String,
    val responsibles: List<String>,
    val responsibilities: List<String>,
    val assignments: List<AssignmentResponse>
)

data class AssignmentResponse(
    val responsible: String,
    val responsibility: String
)

fun Planning.toResponse(): PlanningResponse =
    PlanningResponse(
        periodStart = this.period.start.toString(),
        responsibles = this.responsibles.map { it.name },
        responsibilities = this.responsibilities.map { it.name },
        assignments = this.assignments.map { AssignmentResponse(it.responsible.name, it.responsibility.name) }
    )