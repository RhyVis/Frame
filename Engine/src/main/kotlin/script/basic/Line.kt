package rhx.frame.script.basic

import kotlinx.serialization.Serializable

/**
 * Most basic unit in text presentation.
 */
@Serializable
data class Line(
    val content: String,
) {
    override fun toString(): String = content
}
