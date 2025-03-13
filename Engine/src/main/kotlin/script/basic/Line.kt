package rhx.frame.script.basic

data class Line(
    val content: String,
) {
    override fun toString(): String = content
}
