package cat.ohmushi.kolok.planning

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class KolokApplication

fun main(args: Array<String>) {
    runApplication<KolokApplication>(*args)
}
