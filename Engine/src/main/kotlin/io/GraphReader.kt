package rhx.frame.io

import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import rhx.frame.antlr.graphLexer
import rhx.frame.antlr.graphParser
import rhx.frame.script.graph.node.GraphNodeBuilder
import rhx.frame.script.graph.node.ProgramNode
import java.io.File
import java.io.InputStream

class GraphReader(
    private val stream: InputStream,
) : AutoCloseable {
    constructor(file: File) : this(file.inputStream())

    fun createGraph(): ProgramNode {
        val content = stream.bufferedReader().use { it.readText() }

        val lexer = graphLexer(CharStreams.fromString(content))
        val tokens = CommonTokenStream(lexer)
        val parser = graphParser(tokens)
        val tree = parser.program()
        val visitor = GraphNodeBuilder()

        return visitor.visit(tree) as ProgramNode
    }

    override fun close() {
        stream.close()
    }
}
