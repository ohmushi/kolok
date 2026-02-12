package cat.ohmushi.kolok.planning.adapters.infrastructure

import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

@Component
class JdaDiscordConnexion(
    @Value("\${discord.token}") private val token: String? = null,
) : SmartLifecycle {

    private val logger = KotlinLogging.logger {}

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

        try {
            logger.info { "Starting Discord bot (JDA)..." }

            val built = light(requireNotNull(token) { "Discord token not found." },
                enableCoroutines=true) {
                intents += listOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
            }

            jda = built

            built.awaitReady()
            logger.info { "Discord bot is ready as ${built.selfUser.name}" }
            if (!ready.isCompleted) ready.complete(Unit)
        } catch (e: CancellationException) {
            logger.info(e) { "Discord bot cancelled." }
        } catch (t: Throwable) {
            logger.error(t) { "Discord bot crashed." }
            if (!ready.isCompleted) ready.completeExceptionally(t)
            throw t
        }
    }

    override fun stop() {
        if (!running) return
        running = false

        try {
            jda?.shutdownNow()
        } finally {
            jda = null
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