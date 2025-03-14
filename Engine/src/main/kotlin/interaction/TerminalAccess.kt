package rhx.frame.interaction

import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder

/**
 * Terminal access implementation.
 */
class TerminalAccess : Access {
    private val reader: LineReader = LineReaderBuilder.builder().build()

    override fun display(text: String) {
        print(text)
    }

    override fun displayLn(text: String) {
        println(text)
    }

    override fun waitForInput(prompt: String): String {
        var line: String
        while (true) {
            try {
                line =
                    if (prompt.isNotEmpty()) {
                        reader.readLine(prompt)
                    } else {
                        reader.readLine()
                    }
                return line
            } catch (e: Exception) {
                println("Invalid input. Please try again.")
            }
        }
    }
}
