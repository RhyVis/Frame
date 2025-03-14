package rhx.frame.script.basic

import kotlinx.serialization.Serializable

@Serializable
data class Paragraph(
    val id: UInt,
    val name: String,
) : Iterable<Line> {
    private val lines = mutableListOf<Line>()

    fun addLine(content: String) {
        lines.add(Line(content))
    }

    fun getContent(): List<String> = lines.map { it.toString() }

    override fun iterator(): Iterator<Line> = lines.iterator()
}
