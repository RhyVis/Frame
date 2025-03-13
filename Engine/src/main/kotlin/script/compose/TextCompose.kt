package rhx.frame.script.compose

import rhx.frame.core.graph.Value
import rhx.frame.script.basic.Paragraph

data class TextCompose(
    val name: String,
    val paragraphs: List<Paragraph>,
) {
    private val paragraphByName =
        paragraphs
            .filter { it.name != null }
            .associateBy { it.name!! }

    fun getParagraph(name: String): Paragraph? = paragraphByName[name]

    fun getParagraphContent(name: String, values: Map<String, Value> = emptyMap()): List<String> {
        val paragraph = getParagraph(name) ?: return emptyList()
        return if (values.isEmpty()) {
            paragraph.getContent()
        } else {
            paragraph.getContent().map { replacePlaceholders(it, values) }
        }
    }

    fun getAllParagraphs(values: Map<String, Value> = emptyMap()): List<String> {
        return if (values.isEmpty()) {
            paragraphs.flatMap { it.getContent() }
        } else {
            paragraphs.flatMap { it.getContent().map { content -> replacePlaceholders(content, values) } }
        }
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
