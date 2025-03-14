package rhx.frame.interaction

import rhx.frame.core.graph.Value
import rhx.frame.script.compose.TextCompose

interface Access {
    fun displayCompose(
        compose: TextCompose,
        values: Map<String, Value>,
    ) {
        compose.getAllParagraphs(values).forEach { paragraph ->
            paragraph.forEach {
                displayLn(it)
            }
        }
    }

    fun displayComposePaused(
        compose: TextCompose,
        values: Map<String, Value>,
    ) {
        compose.getAllParagraphs(values).forEach { paragraph ->
            waitForInput()
            paragraph.forEach {
                displayLn(it)
            }
        }
    }

    fun displayComposeRaw(compose: TextCompose) {
        displayCompose(compose, emptyMap())
    }

    fun display(text: String)

    fun displayLn(text: String)

    fun waitForInput(): String

    companion object {
        /**
         * Different implementations of [Access] can be used in different environments.
         */
        lateinit var instance: Access
    }
}
