package rhx.frame.script.basic

import kotlinx.serialization.Serializable

@Serializable
data class Line(
    val content: String,
) {
    override fun toString(): String = content
}
