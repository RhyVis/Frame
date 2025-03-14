package rhx.frame.io

import rhx.frame.script.basic.Paragraph
import rhx.frame.script.compose.Compose
import java.io.File
import java.io.InputStream

class ScriptReader(
    private val stream: InputStream,
) : AutoCloseable {
    constructor(file: File) : this(file.inputStream())

    private var paragraphCounter: UInt = 0u

    fun createTextCompose(): Compose {
        val meta: String
        val content =
            stream.bufferedReader().readLines().let { lines ->
                meta = lines[0].takeIf { it.startsWith('$') }?.trimStart('$')?.trim()
                    ?: throw RuntimeException("Invalid meta")
                lines.drop(1)
            }

        val paragraphs = mutableListOf<Paragraph>()
        var currentParagraph: Paragraph? = null
        var currentParagraphName: String?
        var anonymousCounter = 0

        for (line in content) {
            // Check if this is a paragraph name declaration
            val nameMatch = paragraphNamePattern.find(line)
            val anonymousMatch = anonymousParagraphPattern.find(line)

            if (nameMatch != null) {
                // If we have a current paragraph with content, add it to paragraphs
                if (currentParagraph != null && currentParagraph.iterator().hasNext()) {
                    paragraphs.add(currentParagraph)
                }
                // Create a new paragraph with the name
                currentParagraphName = nameMatch.groupValues[1]
                currentParagraph = Paragraph(paragraphCounter++, currentParagraphName)
            } else if (anonymousMatch != null) {
                // If we have a current paragraph with content, add it to paragraphs
                if (currentParagraph != null && currentParagraph.iterator().hasNext()) {
                    paragraphs.add(currentParagraph)
                }
                // Create a new anonymous paragraph with a generated name
                currentParagraphName = "anonymous_${anonymousCounter++}"
                currentParagraph = Paragraph(paragraphCounter++, currentParagraphName)
            } else if (line.isNotBlank() && currentParagraph != null) {
                // Only add non-blank lines to the current paragraph
                currentParagraph.addLine(line)
            }
        }

        // Add the last paragraph if it exists and has content
        if (currentParagraph != null && currentParagraph.iterator().hasNext()) {
            paragraphs.add(currentParagraph)
        }

        return Compose(meta, paragraphs)
    }

    override fun close() {
        stream.close()
    }

    companion object {
        private val paragraphNamePattern = Regex("^\\[(\\w+)]$")
        private val anonymousParagraphPattern = Regex("^``$")
    }
}
