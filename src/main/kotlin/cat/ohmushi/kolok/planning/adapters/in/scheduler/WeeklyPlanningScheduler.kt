package cat.ohmushi.kolok.planning.adapters.`in`.scheduler

import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.domain.planning.Period
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Component
data class WeeklyPlanningScheduler(
    val generatePlanningUseCase: GeneratePlanningUseCase,
) {

    @Scheduled(cron = "\${planning.scheduler.cron}")
    fun generateNextWeekPlanning() {
        logger.info { "WeeklyPlanningScheduler triggered: generating next week planning." }
        val today = LocalDate.now()
        generatePlanningUseCase.generatePlanning(GeneratePlanningCommand(period = Period.firstAfter(today)))
    }
}