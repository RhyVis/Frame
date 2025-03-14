package rhx.frame.interaction

import rhx.frame.core.graph.Value
import rhx.frame.script.compose.Compose
import rhx.frame.script.compose.MappedParagraph

/**
 * The interface for interaction with the user, and how to display game content.
 *
 * Different implementations of [Access] can be used in different environments.
 */
interface Access {
    fun displayCompose(
        compose: Compose,
        values: Map<String, Value>,
    ) {
        compose.getAllParagraphs(values).forEach { (name, content) ->
            displayLn(name)
            content.forEach { line ->
                displayLn(line)
            }
        }
    }

    fun displayComposePaused(
        compose: Compose,
        values: Map<String, Value>,
    ) {
        compose.getAllParagraphs(values).forEach { (name, content) ->
            displayLn(name)
            waitForInput()
            content.forEach { line ->
                waitForInput()
                displayLn(line)
            }
        }
    }

    fun displayComposeRaw(compose: Compose) {
        displayCompose(compose, emptyMap())
    }

    fun displayParagraph(
        paragraph: MappedParagraph,
    ) {
        paragraph.content.forEach { line ->
            displayLn(line)
        }
    }

    fun displayParagraphRaw(paragraph: MappedParagraph) {
        paragraph.content.forEach { line ->
            displayLn(line)
        }
    }

    fun display(text: String)

    fun displayLn(text: String)

    fun waitForInput(prompt: String = ""): String

    companion object {
        /**
         * The implementation of [Access] to be used.
         */
        lateinit var instance: Access
    }
}
