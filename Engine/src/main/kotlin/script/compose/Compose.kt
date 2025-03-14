package rhx.frame.script.compose

import kotlinx.serialization.Serializable
import rhx.frame.core.graph.Value
import rhx.frame.script.basic.Paragraph

/**
 * The composition of paragraphs, a file will finally be composed of one [Compose].
 *
 * ```
 * $Name_Of_Compose
 *
 * [Paragraph]
 * ...Lines...
 * ```
 *
 * Most text-presentation interactions are based on [Compose], with the functions to
 * override the original content with the values provided.
 *
 * TODO: Add support for multiple composes in a file.
 */
@Serializable
data class Compose(
    val name: String,
    val paragraphs: List<Paragraph>,
) {
    private val paragraphByName = paragraphs.associateBy { it.name }

    private fun getParagraph(name: String): Paragraph? = paragraphByName[name]

    /**
     * Access the content of a paragraph by name.
     *
     * @param name paragraph name
     * @param values values to replace the placeholders
     *
     * @return [MappedParagraph]
     */
    fun getParagraph(
        name: String,
        values: Map<String, Value> = emptyMap(),
    ): MappedParagraph {
        val paragraph = getParagraph(name) ?: return MappedParagraph.EMPTY
        return if (values.isEmpty()) {
            MappedParagraph(paragraph.name, paragraph.getContent())
        } else {
            MappedParagraph(paragraph.name, paragraph.getContent().map { replacePlaceholders(it, values) })
        }
    }

    /**
     * Access the content of a paragraph by id.
     *
     * @param id paragraph id
     * @param values values to replace the placeholders
     *
     * @return [MappedParagraph]
     */
    fun getParagraph(
        id: Int,
        values: Map<String, Value>,
    ): MappedParagraph {
        val paragraph = paragraphs[id]
        return if (values.isEmpty()) {
            MappedParagraph(paragraph.name, paragraph.getContent())
        } else {
            MappedParagraph(paragraph.name, paragraph.getContent().map { replacePlaceholders(it, values) })
        }
    }

    /**
     * Access the content of all paragraphs.
     *
     * @param values values to replace the placeholders
     *
     * @return list of [MappedParagraph]
     */
    fun getAllParagraphs(values: Map<String, Value> = emptyMap()): List<MappedParagraph> =
        if (values.isEmpty()) {
            paragraphs.map { MappedParagraph(it.name, it.getContent()) }
        } else {
            paragraphs.map { MappedParagraph(it.name, it.getContent().map { content -> replacePlaceholders(content, values) }) }
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
