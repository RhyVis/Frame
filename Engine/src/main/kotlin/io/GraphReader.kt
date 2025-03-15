package rhx.frame.io

import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.kotlinruntime.BaseErrorListener
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import rhx.frame.antlr.graphLexer
import rhx.frame.antlr.graphParser
import rhx.frame.exception.GraphParsingException
import rhx.frame.script.graph.GraphNodeBuilder
import rhx.frame.script.graph.ProgramNode
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

        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(GraphErrorListener)
        parser.addErrorListener(GraphErrorListener)

        val tree = parser.program()
        val visitor = GraphNodeBuilder()

        return visitor.visit(tree) as ProgramNode
    }

    override fun close() {
        stream.close()
    }

    companion object {
        object GraphErrorListener : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String,
                e: RecognitionException?,
            ) {
                logger.error { "Caught error syntax: Line $line:$charPositionInLine - $msg" }
                throw GraphParsingException("Line $line:$charPositionInLine - $msg", e)
            }
        }

        private val logger = KotlinLogging.logger("GraphReader")
    }
}
