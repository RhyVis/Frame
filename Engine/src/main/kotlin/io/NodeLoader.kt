package rhx.frame.io

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import rhx.frame.script.graph.node.Node
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
object NodeLoader {
    private const val BINARY_EXT_NAME = "frd"

    private val logger = KotlinLogging.logger("NodeLoader")

    fun saveNode(
        node: Node,
        path: Path,
    ) {
        val bytes = ProtoBuf.encodeToByteArray(node)
        path.outputStream().use { it.write(bytes) }
    }

    fun loadNode(path: Path): Node? {
        if (!path.exists()) return null

        return try {
            val bytes = Files.readAllBytes(path)
            ProtoBuf.decodeFromByteArray<Node>(bytes)
        } catch (e: Exception) {
            logger.error(e) { "Failed to load node from $path" }
            null
        }
    }
}
