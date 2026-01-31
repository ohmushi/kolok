package cat.ohmushi.kolok.planning.adapters.`in`.scheduler

import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.domain.planning.Period
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

const val EVERY_SUNDAY_AT_6_PM = "0 0 18 * * SUN"

@Component
data class WeeklyPlanningScheduler(
    val generatePlanningUseCase: GeneratePlanningUseCase,
) {

    @Scheduled(cron = EVERY_SUNDAY_AT_6_PM, zone = "Europe/Paris")
    fun generateNextWeekPlanning() {
        val today = LocalDate.now()
        generatePlanningUseCase.generatePlanning(GeneratePlanningCommand(period = Period.firstAfter(today)))
    }
}