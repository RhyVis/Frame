package rhx.frame.script.compose

import kotlinx.serialization.Serializable

/**
 * Mapped paragraph with replaced placeholders.
 */
@Serializable
data class MappedParagraph(
    val name: String,
    val content: List<String>,
) {
    companion object {
        val EMPTY = MappedParagraph("", emptyList())
    }
}
