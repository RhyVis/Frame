package rhx.frame.script.basic

data class Paragraph(
    val id: UInt,
    val name: String? = null,
) : Iterable<Line> {
    private val lines = mutableListOf<Line>()

    fun addLine(content: String) {
        lines.add(Line(content))
    }

    fun getContent(): List<String> = lines.map { it.toString() }

    override fun iterator(): Iterator<Line> = lines.iterator()
}
