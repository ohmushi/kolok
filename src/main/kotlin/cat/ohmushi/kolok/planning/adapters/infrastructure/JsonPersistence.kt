package cat.ohmushi.kolok.planning.adapters.infrastructure

import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import tools.jackson.module.kotlin.readValue
import kotlin.io.path.absolutePathString

data class JsonFile(
    val absences: List<AbsenceFileEntry> = emptyList(),
    val responsibilityVersions: List<ResponsibilitiesVersionFileEntry> = emptyList(),
    val users: List<User> = emptyList(),
)

data class AbsenceFileEntry(
    val responsible: String,
    val from: String,
    val periodsCount: Int,
)

data class ResponsibilitiesVersionFileEntry(
    val from: String,
    val responsibilities: List<String>
)

data class User(
    val responsible: String,
    val id: String,
)

class JsonPersistence(
    val path: Path,
    private val mapper: ObjectMapper
) {
    fun read(): JsonFile {
        if (!Files.exists(path)) throw Exception("File [${path.absolutePathString()}] doesn't exist.")
        return Files.newBufferedReader(path).use { reader -> mapper.readValue(reader) }
    }

    fun write(catalog: JsonFile) {
        Files.createDirectories(path.parent)
        Files.newBufferedWriter(path).use { writer -> mapper.writerWithDefaultPrettyPrinter().writeValue(writer, catalog) }
    }
}
