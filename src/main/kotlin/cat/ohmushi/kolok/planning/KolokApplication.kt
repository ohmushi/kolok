package cat.ohmushi.kolok.planning

import cat.ohmushi.kolok.planning.application.annotations.ApplicationComponent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan(
    basePackages = ["domain", "application"],
    includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [ApplicationComponent::class])]
)
class KolokApplication

fun main(args: Array<String>) {
    runApplication<KolokApplication>(*args)
}
