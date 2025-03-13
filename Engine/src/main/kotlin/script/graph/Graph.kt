package rhx.frame.script.graph

import rhx.frame.init.TextDict
import rhx.frame.script.compose.TextCompose

data class Graph(
    val name: String,
    private val refs: List<String>,
) : Iterable<TextCompose> {
    override fun iterator(): Iterator<TextCompose> =
        object : Iterator<TextCompose> {
            private val refIter = refs.iterator()

            override fun hasNext(): Boolean = refIter.hasNext()

            override fun next(): TextCompose = TextDict[refIter.next()] ?: throw NoSuchElementException("Invalid ref")
        }

    operator fun get(index: Int): TextCompose = TextDict[refs[index]] ?: throw NoSuchElementException("Invalid ref")

    fun validate() {
        refs.forEach { if (it !in TextDict) throw RuntimeException("Invalid ref $it in graph $name") }
    }
}
