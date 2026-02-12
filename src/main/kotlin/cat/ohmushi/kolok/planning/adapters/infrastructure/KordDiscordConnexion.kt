package cat.ohmushi.kolok.planning.adapters.infrastructure

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

@Component
class KordDiscordConnexion(
    @Value("\${discord.token}") private val token: String? = null,
) : SmartLifecycle {

    private val logger = KotlinLogging.logger {}

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var kord: Kord? = null
    private var job: Job? = null

    @Volatile
    private var running: Boolean = false
    override fun isRunning(): Boolean = running

    override fun isAutoStartup(): Boolean = true

    @Volatile
    private var ready: CompletableDeferred<Unit> = CompletableDeferred()

    override fun stop() {
        if (!running) return
        running = false
        job?.cancel()
        job = null
        kord = null
        scope.cancel()
    }

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun start() {
        if (running) return
        running = true

        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        ready = CompletableDeferred()

        job = scope.launch {
            try {
                logger.info { "Starting Discord bot..." }
                val k = Kord(requireNotNull(token) { "Discord token not found." })
                kord = k

                k.on<ReadyEvent> {
                    val username = k.getSelf().username
                    logger.info { "Discord bot is ready as $username" }
                    if (!ready.isCompleted) ready.complete(Unit)
                }

                k.login {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intent.MessageContent
                }
            } catch (e: CancellationException) {
                logger.info(e) { "Discord bot cancelled." }
            } catch (t: Throwable) {
                logger.error(t) { "Discord bot crashed." }
                if (!ready.isCompleted) ready.completeExceptionally(t)
                throw t
            }
        }
    }

    suspend fun <T> withKord(block: suspend (Kord) -> T): T {
        ready.await()
        val k = kord ?: error("Kord not initialized")
        return block(k)
    }
}