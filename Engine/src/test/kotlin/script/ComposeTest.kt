package script

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import rhx.frame.core.getCompose
import rhx.frame.core.graph.SystemEnv
import rhx.frame.init.AccessLoader
import rhx.frame.init.DataLoader
import kotlin.test.Test

class ComposeTest {
    @Test
    fun testParseParagraph() {
        println()
        println("==== Testing Parse Paragraph ====")

        val compose = getCompose("test_compose")

        val string = Json.encodeToString(compose)
        println(string)

        assert(compose.paragraphs.size == 6)
        assert(compose.paragraphs[0].id == 0u)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            println("==== Setup Environment ====")
            try {
                AccessLoader.load()
                SystemEnv.load()
                DataLoader.loadFromClasspath()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }
}
