package rhx.frame.script.basic

import kotlinx.serialization.Serializable

/**
 * Unit of text presentation.
 *
 * ```
 * [Paragraph 1]
 * ...Defines a paragraph...
 *
 * ``
 * ...Defines an anonymous paragraph...
 * ```
 *
 * A paragraph is a collection of [Line]. Starts with the declaration
 * and ends with the next declaration or the end of the file.
 *
 * The anonymous paragraph will also have a name property given by the parser,
 * when paragraph name is not necessary in the context.
 */
@Serializable
data class Paragraph(
    val id: UInt,
    val name: String,
) : Iterable<Line> {
    private val lines = mutableListOf<Line>()

    /**
     * Add a line to the paragraph.
     *
     * @param content line content
     */
    fun addLine(content: String) {
        lines.add(Line(content))
    }

    /**
     * Directly map the content of the paragraph.
     *
     * @return list of raw strings
     */
    fun getContent(): List<String> = lines.map { it.toString() }

    override fun iterator(): Iterator<Line> = lines.iterator()
}
