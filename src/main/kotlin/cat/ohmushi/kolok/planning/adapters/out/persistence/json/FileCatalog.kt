package cat.ohmushi.kolok.planning.adapters.out.persistence.json


import CatalogFile
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import tools.jackson.module.kotlin.readValue

class FileCatalog(
    val path: Path,
    private val mapper: ObjectMapper
) {
    fun read(): CatalogFile {
        if (!Files.exists(path)) return CatalogFile()
        return Files.newBufferedReader(path).use { reader -> mapper.readValue(reader) }
    }

    fun write(catalog: CatalogFile) {
        Files.createDirectories(path.parent)
        Files.newBufferedWriter(path).use { writer -> mapper.writerWithDefaultPrettyPrinter().writeValue(writer, catalog) }
    }

    fun resolveByPeriod(
        periodStart: LocalDate,
        byPeriod: Map<String, List<String>>
    ): List<String> {
        if (byPeriod.isEmpty()) return emptyList()

        val entries = byPeriod.entries
            .mapNotNull { (k, v) ->
                runCatching { LocalDate.parse(k) }.getOrNull()?.let { it to v }
            }
            .sortedBy { it.first }

        val exact = entries.firstOrNull { it.first == periodStart }?.second
        if (exact != null) return exact

        return entries.lastOrNull { it.first.isBefore(periodStart) }?.second ?: emptyList()
    }
}
