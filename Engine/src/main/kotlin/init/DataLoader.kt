package rhx.frame.init

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.core.graph.GlobalEnv
import rhx.frame.exception.RuntimeIncompleteException
import rhx.frame.io.GraphReader
import rhx.frame.io.ScriptReader
import rhx.frame.script.compose.TextCompose
import rhx.frame.script.graph.node.GlobalFunctionDeclaration
import rhx.frame.script.graph.node.ObjectTypeDeclaration
import java.io.File
import java.nio.file.Path
import kotlin.time.measureTime

object DataLoader {
    private const val COMPOSE_DIR = "compose"
    private const val GRAPH_DIR = "graph"

    private const val TEXT_COMPOSE_EXT_NAME = "frt"
    private const val GRAPH_EXT_NAME = "frg"

    private val logger = KotlinLogging.logger("DataLoader")

    fun load(rootDir: Path? = null) =
        measureTime {
            reset()
            loadCompose(rootDir)
            loadGraph(rootDir)
        }.also {
            logger.info { "Data loaded in ${it.inWholeMilliseconds} ms." }
        }

    fun loadFromClasspath() =
        measureTime {
            reset()
            val classLoader = Thread.currentThread().contextClassLoader ?: DataLoader::class.java.classLoader
            loadCompose(classLoader)
            loadGraph(classLoader)
        }.also {
            logger.info { "Data loaded in ${it.inWholeMilliseconds} ms." }
        }

    private fun loadCompose(rootDir: Path?) {
        val composeDir = if (rootDir == null) File(COMPOSE_DIR) else rootDir.resolve(COMPOSE_DIR).toFile()
        if (!composeDir.exists() || !composeDir.isDirectory) {
            throw RuntimeIncompleteException("Compose directory does not exist or is not a directory.")
        }
        walkComposeDir(composeDir)
    }

    private fun loadGraph(rootDir: Path?) {
        val graphDir = if (rootDir == null) File(GRAPH_DIR) else rootDir.resolve(GRAPH_DIR).toFile()
        if (!graphDir.exists() || !graphDir.isDirectory) {
            throw RuntimeIncompleteException("Graph directory does not exist or is not a directory.")
        }
        walkGraphDir(graphDir)
    }

    private fun loadCompose(classLoader: ClassLoader) {
        try {
            val resourceUrls = classLoader.getResources("$COMPOSE_DIR/")
            while (resourceUrls.hasMoreElements()) {
                val resourceUrl = resourceUrls.nextElement()
                val resourcePath = resourceUrl.path
                val resourceFile = File(resourcePath)

                if (resourceFile.exists() || resourceFile.isDirectory) {
                    walkComposeDir(resourceFile)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load TextCompose from classpath." }
        }
    }

    private fun loadGraph(classLoader: ClassLoader) {
        try {
            val resourceUrls = classLoader.getResources("$GRAPH_DIR/")
            while (resourceUrls.hasMoreElements()) {
                val resourceUrl = resourceUrls.nextElement()
                val resourcePath = resourceUrl.path
                val resourceFile = File(resourcePath)

                if (resourceFile.exists() || resourceFile.isDirectory) {
                    walkGraphDir(resourceFile)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load Graph from classpath." }
        }
    }

    private fun walkComposeDir(dir: File) {
        dir.walkTopDown().filter { it.isFile && it.extension == TEXT_COMPOSE_EXT_NAME }.forEach { file ->
            logger.debug { "Parsing ${file.name} as compose." }
            try {
                processComposeFile(file)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load TextCompose from ${file.absolutePath}" }
            }
        }
    }

    private fun walkGraphDir(dir: File) {
        dir.walkTopDown().filter { it.isFile && it.extension == GRAPH_EXT_NAME }.forEach { file ->
            logger.debug { "Parsing ${file.name} as graph." }
            try {
                processGraphFile(file)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load Graph from ${file.absolutePath}" }
            }
        }
    }

    private fun processComposeFile(file: File) {
        ScriptReader(file).use { reader ->
            val textCompose: TextCompose = reader.createTextCompose()
            if (textCompose.name in TextDict) {
                logger.warn {
                    "Duplicate crush of ${textCompose.name} by ${file.absolutePath}, overriding."
                }
            }
            TextDict[textCompose.name] = textCompose
        }
    }

    private fun processGraphFile(file: File) {
        GraphReader(file).use { reader ->
            val graph = reader.createGraph()

            graph.statements.filterIsInstance<GlobalFunctionDeclaration>().forEach {
                logger.debug { "Preloading Global Function Declaration ${it.name}" }
                if (GlobalEnv.hasFunction(it.name)) {
                    logger.warn { "Global function ${it.name} shadowed in ${graph.name}" }
                }
                GlobalEnv.declareGlobalFunction(it.name, it.toFunctionDeclaration())
            }

            graph.statements.filterIsInstance<ObjectTypeDeclaration>().forEach {
                logger.debug { "Preloading Object Type Declaration ${it.name}" }
                if (GlobalEnv.hasObjectType(it.name)) {
                    logger.warn { "Object type ${it.name} shadowed in ${graph.name}" }
                }
                GlobalEnv.declareObjectType(it.name, it)
            }

            GraphDict[graph.name] = graph
        }
    }

    private fun reset() {
        TextDict.clear()
        GraphDict.clear()
        GlobalEnv.clear()
    }
}
