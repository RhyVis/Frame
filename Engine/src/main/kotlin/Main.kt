package rhx.frame

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.core.EnginePlayer
import rhx.frame.core.graph.SystemEnv
import rhx.frame.init.AccessLoader
import rhx.frame.init.DataLoader

fun main() {
    GlobalLogger.info { "Frame Engine initializing." }

    AccessLoader.load()
    SystemEnv.load()
    DataLoader.load()

    println()

    val player = EnginePlayer()
}

internal val GlobalLogger = KotlinLogging.logger("Engine")
