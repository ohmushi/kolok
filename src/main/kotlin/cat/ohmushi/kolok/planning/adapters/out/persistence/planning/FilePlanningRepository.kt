package cat.ohmushi.kolok.planning.adapters.out.persistence.planning

import cat.ohmushi.kolok.planning.application.ports.out.PlanningRepository
import cat.ohmushi.kolok.planning.domain.Period
import cat.ohmushi.kolok.planning.domain.Responsibility
import cat.ohmushi.kolok.planning.domain.Responsible
import cat.ohmushi.kolok.planning.domain.planning.Assignment
import cat.ohmushi.kolok.planning.domain.planning.Planning
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

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


class FilePlanningRepository(
    private val path: Path,
    private val mapper: ObjectMapper
) : PlanningRepository {

    override fun findLatestBefore(period: Period): Planning? {
        val all = readAll()
        return all
            .filter { it.period.start.isBefore(period.start) }
            .maxByOrNull { it.period.start }
    }

    override fun save(planning: Planning) {
        val all = readAll().toMutableList()
        all.removeIf { it.period == planning.period }
        all += planning
        writeAll(all)
    }

    override fun findFor(period: Period): Planning? {
        return readAll().find { it.period == period }
    }

    private fun readAll(): List<Planning> {
        if (!Files.exists(path)) return emptyList()

        val entries: List<PlanningFileEntry> = Files.newBufferedReader(path).use { r -> mapper.readValue(r) }

        return entries.map { e ->
            Planning(
                period = Period(LocalDate.parse(e.periodStart)),
                responsibles = e.responsibles.map { Responsible(it) },
                responsibilities = e.responsibilities.map { Responsibility(it) },
                assignments = e.assignments.map { a ->
                    Assignment(
                        responsible = Responsible(a.responsible),
                        responsibility = Responsibility(a.responsibility)
                    )
                }
            )
        }
    }

    private fun writeAll(plannings: List<Planning>) {
        Files.createDirectories(path.parent)

        val entries = plannings
            .sortedBy { it.period.start }
            .map { p ->
                PlanningFileEntry(
                    periodStart = p.period.start.toString(),
                    responsibles = p.responsibles.map { it.name },
                    responsibilities = p.responsibilities.map { it.name },
                    assignments = p.assignments.map { a ->
                        AssignmentFileEntry(
                            responsible = a.responsible.name,
                            responsibility = a.responsibility.name
                        )
                    }
                )
            }

        Files.newBufferedWriter(path).use { w -> mapper.writerWithDefaultPrettyPrinter().writeValue(w, entries) }
    }
}