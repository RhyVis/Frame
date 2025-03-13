package rhx.frame

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.core.EnginePlayer
import rhx.frame.core.graph.ExecMachine
import rhx.frame.init.DataLoader
import rhx.frame.init.GraphDict
import rhx.frame.init.TextDict

fun main() {
    GlobalLogger.info { "Frame Engine initializing." }

    DataLoader.load()

    println()

    val player = EnginePlayer()
}

internal val GlobalLogger = KotlinLogging.logger("Engine")
