package cat.ohmushi.kolok.planning.adapters.`in`.scheduler

import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningCommand
import cat.ohmushi.kolok.planning.application.ports.`in`.planning.GeneratePlanningUseCase
import cat.ohmushi.kolok.planning.adapters.infrastructure.JdaDiscordConnexion
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Duration

@SpringBootTest
@EnableScheduling
@TestPropertySource(properties = ["planning.scheduler.cron=0/1 * * * * *"])
class WeeklyPlanningSchedulerTest {

    @MockitoBean
    private lateinit var jdaDiscordConnexion: JdaDiscordConnexion

    @MockitoBean
    private lateinit var generatePlanningUseCase: GeneratePlanningUseCase

    @Autowired
    private lateinit var scheduler: WeeklyPlanningScheduler

    @Test
    fun `generateNextWeekPlanning should be triggered based on the planning scheduler cron property`() {
        // The cron is set to every second in @TestPropertySource
        // Wait up to 2 seconds for the scheduled method to be called
        await()
            .timeout(Duration.ofSeconds(2))
            .untilAsserted {
                verify(generatePlanningUseCase).generatePlanning(any<GeneratePlanningCommand>())
            }
    }

    @Test
    fun `generateNextWeekPlanning should not call generatePlanning immediately upon component initialization`() {
        verify(generatePlanningUseCase, never()).generatePlanning(any<GeneratePlanningCommand>())
    }
}

