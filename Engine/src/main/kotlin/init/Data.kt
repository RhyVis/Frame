package rhx.frame.init

import rhx.frame.script.compose.TextCompose
import rhx.frame.script.graph.node.ProgramNode

/**
 * TextDict is a map of text name to text object.
 */
val TextDict = mutableMapOf<String, TextCompose>()

fun getCompose(name: String): TextCompose = TextDict[name] ?: throw IllegalArgumentException("Compose $name not found")

/**
 * GraphDict is a map of graph name to graph object.
 */
val GraphDict = mutableMapOf<String, ProgramNode>()

fun getGraph(name: String): ProgramNode = GraphDict[name] ?: throw IllegalArgumentException("Graph $name not found")
