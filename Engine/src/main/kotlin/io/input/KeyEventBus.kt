package rhx.frame.io.input

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object KeyEventBus {
    private val eventQueue = LinkedBlockingQueue<KeyEvent>()
    private val listeners = ConcurrentHashMap<String, (KeyEvent) -> Unit>()
    private var inputJob: Job? = null
    private var processingJob: Job? = null

    private val logger = KotlinLogging.logger("KeyEventBus")

    fun start() {
        if (inputJob != null) return

        inputJob =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    while (isActive) {
                        val keyChar = System.`in`.read().toChar()
                        eventQueue.add(KeyEvent(KeyEventType.PRESS, keyChar))
                        eventQueue.add(KeyEvent(KeyEventType.RELEASE, keyChar))
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Key input listener stooped due to exception" }
                }
            }

        processingJob =
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    while (isActive) {
                        val event = eventQueue.take()
                        listeners.values.forEach { it(event) }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Key event processing stooped due to exception" }
                }
            }
    }

    fun stop() {
        inputJob?.cancel()
        processingJob?.cancel()
        inputJob = null
        processingJob = null
    }

    fun registerListener(
        name: String,
        listener: (KeyEvent) -> Unit,
    ) {
        listeners[name] = listener
    }

    fun unregisterListener(name: String) {
        listeners.remove(name)
    }

    fun waitForKeyPress(timeout: Long = Long.MAX_VALUE): Char? {
        var result: Char? = null
        val latch = CountDownLatch(1)

        val listenerId = "waitForKey-${System.currentTimeMillis()}"
        registerListener(listenerId) { event ->
            if (event.type == KeyEventType.PRESS) {
                result = event.keyChar
                latch.countDown()
            }
        }

        try {
            latch.await(timeout, TimeUnit.MILLISECONDS)
            return result
        } finally {
            unregisterListener(listenerId)
        }
    }
}
