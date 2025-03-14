package rhx.frame.script.compose

import kotlinx.serialization.Serializable
import rhx.frame.core.graph.Value
import rhx.frame.script.basic.Paragraph

@Serializable
data class TextCompose(
    val name: String,
    val paragraphs: List<Paragraph>,
) {
    private val paragraphByName = paragraphs.associateBy { it.name }

    private fun getParagraph(name: String): Paragraph? = paragraphByName[name]

    fun getParagraphContent(
        name: String,
        values: Map<String, Value> = emptyMap(),
    ): List<String> {
        val paragraph = getParagraph(name) ?: return emptyList()
        return if (values.isEmpty()) {
            paragraph.getContent()
        } else {
            paragraph.getContent().map { replacePlaceholders(it, values) }
        }
    }

    fun getAllParagraphs(values: Map<String, Value> = emptyMap()): List<List<String>> =
        if (values.isEmpty()) {
            paragraphs.map { it.getContent() }
        } else {
            paragraphs.map { it.getContent().map { content -> replacePlaceholders(content, values) } }
        }

    companion object {
        fun replacePlaceholders(
            text: String,
            values: Map<String, Value>,
        ): String {
            var result = text
            values.forEach { (k, v) -> result = result.replace("{$k}", v.toString()) }
            return result
        }
    }
}
