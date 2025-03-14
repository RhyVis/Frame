package script

import org.junit.jupiter.api.BeforeAll
import rhx.frame.core.graph.ExecMachine
import rhx.frame.core.graph.ScriptException
import rhx.frame.core.graph.SystemEnv
import rhx.frame.init.AccessLoader
import rhx.frame.init.DataLoader
import rhx.frame.init.GraphDict
import kotlin.test.Test
import kotlin.test.expect
import kotlin.time.measureTime

class GraphExecTest {
    @Test
    fun testLoop() {
        println()
        println("==== Testing Loop ====")
        val loopGraph = GraphDict["test_loop"] ?: error("Graph test_loop not found")
        ExecMachine.use {
            measureTime {
                it.execScope(loopGraph.statements)
            }.also {
                println("Executed in ${it.inWholeMilliseconds} ms.")
            }
        }
    }

    @Test
    fun testIf() {
        println()
        println("==== Testing If ====")
        val ifGraph = GraphDict["test_if"] ?: error("Graph test_if not found")
        ExecMachine.use {
            it.execScope(ifGraph.statements)
        }
    }

    @Test
    fun testExcept() {
        println()
        println("==== Testing Except ====")
        val exceptGraph = GraphDict["test_except"] ?: error("Graph test_except not found")
        try {
            ExecMachine.use {
                it.execScope(exceptGraph.statements)
            }
        } catch (e: ScriptException) {
            println("Caught exception: '${e.message}'")
            expect(e.message) { "Fail" }
            println("Success")
        }
    }

    @Test
    fun testJump() {
        println()
        println("==== Testing Jump ====")
        val jumpGraph = GraphDict["test_jump"] ?: error("Graph test_jump not found")
        ExecMachine.use {
            it.execScope(jumpGraph.statements)
        }
    }

    @Test
    fun testObject() {
        println()
        println("==== Testing Object ====")
        val objectGraph = GraphDict["test_object"] ?: error("Graph test_object not found")
        ExecMachine.use {
            it.execScope(objectGraph.statements)
        }
    }

    @Test
    fun testGlobalFunc() {
        println()
        println("==== Testing Global Function ====")
        val globalFuncGraph = GraphDict["test_global_function_call"] ?: error("Graph test_global_function_call not found")
        ExecMachine.use {
            it.execScope(globalFuncGraph.statements)
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            println("==== Setup Environment ====")
            try {
                AccessLoader.load()
                SystemEnv.load()
                DataLoader.loadFromClasspath()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }
}
