package rhx.frame.core

import rhx.frame.core.graph.ExecMachine

class EnginePlayer {
    init {
        val mainGraph = getGraph("main")
        ExecMachine.use {
            it.execScope(mainGraph.statements)
        }
    }
}
