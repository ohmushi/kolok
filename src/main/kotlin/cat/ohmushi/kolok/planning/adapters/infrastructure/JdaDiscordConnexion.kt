package cat.ohmushi.kolok.planning.adapters.infrastructure

import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

@Component
class JdaDiscordConnexion(
    @Value("\${discord.token}") private val token: String? = null,
) : SmartLifecycle {

    private val logger by SLF4J
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    @Volatile
    private var running: Boolean = false

    @Volatile
    private var ready: CompletableDeferred<Unit> = CompletableDeferred()

    @Volatile
    private var jda: JDA? = null



    override fun isRunning(): Boolean = running

    override fun isAutoStartup(): Boolean = true

    override fun start() {
        if (running) return
        running = true
        ready = CompletableDeferred()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        job = scope.launch {
            try {
                logger.info( "Starting Discord bot (JDA)...")

                val built = light(requireNotNull(token) { "Discord token not found." },
                    enableCoroutines=true)

                jda = built

                built.awaitReady()
                logger.info( "Discord bot is ready as ${built.selfUser.name}")
                if (!ready.isCompleted) ready.complete(Unit)
            } catch (e: CancellationException) {
                logger.info("Discord bot cancelled.", e)
            } catch (t: Throwable) {
                logger.error("Discord bot crashed." , t)
                if (!ready.isCompleted) ready.completeExceptionally(t)
                throw t
            }
        }
    }

    override fun stop() {
        if (!running) return
        running = false

        try {
            jda?.shutdownNow()
            scope.cancel()
        } finally {
            jda = null
            job = null
        }
    }

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    suspend fun <T> withJda(block: (JDA) -> T): T {
        ready.await()
        val client = jda ?: error("JDA not initialized")
        return block(client)
    }
}