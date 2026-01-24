package cat.ohmushi.kolok.planning

import cat.ohmushi.kolok.planning.application.annotations.ApplicationService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan(
    basePackages = ["cat.ohmushi.kolok.planning"],
    includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [ApplicationService::class])],
)
class KolokApplication

fun main(args: Array<String>) {
    runApplication<KolokApplication>(*args)
}
