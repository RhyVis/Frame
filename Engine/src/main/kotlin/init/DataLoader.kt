package rhx.frame.init

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.core.GraphDict
import rhx.frame.core.TextDict
import rhx.frame.core.graph.GlobalEnv
import rhx.frame.core.graph.SystemEnv
import rhx.frame.exception.RuntimeIncompleteException
import rhx.frame.io.GraphReader
import rhx.frame.io.NodeLoader
import rhx.frame.io.ScriptReader
import rhx.frame.script.compose.Compose
import rhx.frame.script.graph.GlobalFunctionDeclaration
import rhx.frame.script.graph.ObjectTypeDeclaration
import rhx.frame.script.graph.ProgramNode
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.time.measureTime

/**
 * Load data from file system or classpath. Right after the initialization of [SystemEnv].
 *
 * All compose and graph files are loaded and cached in memory.
 * Pre-serialize and cache graph nodes to speed up loading.
 *
 * @see ScriptReader
 * @see GraphReader
 * @see NodeLoader
 */
object DataLoader {
    private const val COMPOSE_DIR = "compose"
    private const val GRAPH_DIR = "graph"

    private const val COMPOSE_EXT_NAME = "frc"
    private const val GRAPH_EXT_NAME = "frg"
    private const val BINARY_EXT_NAME = "frb"

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
        dir.walkTopDown().filter { it.isFile && it.extension == COMPOSE_EXT_NAME }.forEach { file ->
            try {
                processComposeFile(file)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load TextCompose from ${file.absolutePath}" }
            }
        }
    }

    private fun walkGraphDir(dir: File) {
        dir.walkTopDown().filter { it.isFile && it.extension == GRAPH_EXT_NAME }.forEach { file ->
            try {
                processGraphFile(file)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load Graph from ${file.absolutePath}" }
            }
        }
    }

    private fun processComposeFile(file: File) {
        ScriptReader(file).use { reader ->
            val compose: Compose = reader.createTextCompose()
            if (compose.name in TextDict) {
                logger.warn {
                    "Duplicate crush of ${compose.name} by ${file.absolutePath}, overriding."
                }
            }
            TextDict[compose.name] = compose
        }
    }

    private fun processGraphFile(file: File) {
        val sourcePath = file.toPath()
        val binaryPath = getBinaryPath(sourcePath)

        if (isBinaryUpToDate(sourcePath, binaryPath)) {
            logger.debug { "Loading graph from binary cache: $binaryPath" }
            val node = NodeLoader.loadNode(binaryPath)
            if (node is ProgramNode) {
                registerGraphNode(node)
                GraphDict[node.name] = node
                return
            } else {
                logger.warn { "Binary cache invalid for ${file.name}, falling back to parsing" }
            }
        }

        GraphReader(file).use { reader ->
            val graph = reader.createGraph()
            registerGraphNode(graph)
            GraphDict[graph.name] = graph

            try {
                NodeLoader.saveNode(graph, binaryPath)
                logger.debug { "Saved binary cache for ${file.name}" }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to save binary cache for ${file.name}" }
            }
        }
    }

    private fun registerGraphNode(graph: ProgramNode) {
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
    }

    private fun getBinaryPath(sourcePath: Path): Path {
        val sourcePathStr = sourcePath.toString()
        val binaryPathStr = sourcePathStr.substring(0, sourcePathStr.length - GRAPH_EXT_NAME.length) + BINARY_EXT_NAME
        return Path.of(binaryPathStr)
    }

    private fun isBinaryUpToDate(
        sourcePath: Path,
        binaryPath: Path,
    ): Boolean {
        if (!binaryPath.exists()) return false
        val sourceLastModified = sourcePath.getLastModifiedTime()
        val binaryLastModified = binaryPath.getLastModifiedTime()

        return sourceLastModified < binaryLastModified
    }

    private fun reset() {
        TextDict.clear()
        GraphDict.clear()
        GlobalEnv.clear()
    }
}
