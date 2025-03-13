package rhx.frame.io

import rhx.frame.script.basic.Paragraph
import rhx.frame.script.compose.TextCompose
import java.io.File
import java.io.InputStream

class ScriptReader(
    private val stream: InputStream,
) : AutoCloseable {
    constructor(file: File) : this(file.inputStream())

    private var paragraphCounter: UInt = 0u
    private val paragraphNamePattern = Regex("^\\[(\\w+)]$")

    fun createTextCompose(): TextCompose {
        val meta: String
        val content =
            stream.bufferedReader().readLines().let { lines ->
                meta = lines[0].takeIf { it.startsWith('$') }?.trimStart('$')?.trim()
                    ?: throw RuntimeException("Invalid meta")
                lines.drop(1)
            }

        val paragraphs = mutableListOf<Paragraph>()
        var currentParagraph = Paragraph(paragraphCounter++)
        var currentParagraphName: String?

        for (line in content) {
            if (line.isBlank()) {
                if (currentParagraph.iterator().hasNext()) {
                    paragraphs.add(currentParagraph)
                    currentParagraph = Paragraph(paragraphCounter++)
                }
            } else {
                // Check if this is a paragraph name declaration
                val nameMatch = paragraphNamePattern.find(line)
                if (nameMatch != null) {
                    // If we have content in the current paragraph, add it and create new
                    if (currentParagraph.iterator().hasNext()) {
                        paragraphs.add(currentParagraph)
                    }
                    currentParagraphName = nameMatch.groupValues[1]
                    currentParagraph = Paragraph(paragraphCounter++, currentParagraphName)
                } else {
                    currentParagraph.addLine(line)
                }
            }
        }

        if (currentParagraph.iterator().hasNext()) {
            paragraphs.add(currentParagraph)
        }

        return TextCompose(meta, paragraphs)
    }

    override fun close() {
        stream.close()
    }
}
