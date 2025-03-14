package rhx.frame.core

import rhx.frame.script.compose.Compose
import rhx.frame.script.graph.ProgramNode

/**
 * TextDict is a map of text name to text object.
 */
val TextDict = mutableMapOf<String, Compose>()

fun getCompose(name: String): Compose = TextDict[name] ?: throw IllegalArgumentException("Compose $name not found")

/**
 * GraphDict is a map of graph name to graph object.
 */
val GraphDict = mutableMapOf<String, ProgramNode>()

fun getGraph(name: String): ProgramNode = GraphDict[name] ?: throw IllegalArgumentException("Graph $name not found")
