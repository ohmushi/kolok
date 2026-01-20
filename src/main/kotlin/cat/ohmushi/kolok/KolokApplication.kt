package cat.ohmushi.kolok

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class KolokApplication

fun main(args: Array<String>) {
    runApplication<KolokApplication>(*args)
}
