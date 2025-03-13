package rhx.frame.core

import rhx.frame.core.graph.ExecMachine
import rhx.frame.init.getGraph

class EnginePlayer {
    init {
        val mainGraph = getGraph("main")
        ExecMachine.use {
            it.execScope(mainGraph.statements)
        }
    }
}
