package rhx.frame.core.graph

import rhx.frame.core.ScriptException
import rhx.frame.init.DataLoader
import rhx.frame.interaction.Access

/**
 * System environment, more like static information and functions. Make sure to initialize it before [DataLoader].
 */
object SystemEnv {
    /**
     * System function map
     *
     * @see systemCall
     */
    private lateinit var systemCallMap: MutableMap<String, (List<Value>) -> Value>

    private val access by lazy { Access.instance }

    /**
     * Initialize [systemCallMap] and register system functions
     *
     * @see systemCall
     */
    fun load() {
        systemCallMap =
            mutableMapOf(
                "except" to except,
                "to_string" to toStringSys,
                "print" to print,
                "println" to printLn,
                "printf" to printFormat,
                "wait_input" to waitInput,
            )
    }

    /**
     * Call system function by name
     *
     * @param name function name
     * @param args function arguments
     * @return function result
     *
     * @throws IllegalArgumentException if function not found
     */
    fun systemCall(
        name: String,
        args: List<Value>,
    ): Value {
        val func =
            systemCallMap[name]
                ?: throw IllegalArgumentException("System function $name not found")

        return func(args)
    }

    /**
     * Register system function, use after [load]
     *
     * @param name function name
     * @param func function implementation
     */
    fun registerSystemCall(
        name: String,
        func: (List<Value>) -> Value,
    ) {
        systemCallMap[name] = func
    }

    // region System functions

    /**
     * Throw exception with message
     */
    private val except = { args: List<Value> ->
        throw ScriptException(args.joinToString(" ") { it.toString() })
    }

    /**
     * Convert value to [Value.StringValue]
     */
    private val toStringSys = { args: List<Value> ->
        Value.createString(args[0].toString())
    }

    /**
     * Print function
     */
    private val print = { args: List<Value> ->
        access.display(args.joinToString(" ") { it.toString() })
        Value.VOID
    }

    /**
     * Print line function
     */
    private val printLn = { args: List<Value> ->
        access.displayLn(args.joinToString(" ") { it.toString() })
        Value.VOID
    }

    /**
     * Print format function
     */
    private val printFormat = { args: List<Value> ->
        val format = args[0].toString()
        val values = args.drop(1).map { it.toString() }.toTypedArray()
        access.displayLn(format.format(*values))
        Value.VOID
    }

    /**
     * Wait for input function, returns input string
     */
    private val waitInput = { args: List<Value> ->
        val prompt = if (args.isNotEmpty()) args[0].toString() else ""
        val key = access.waitForInput(prompt)
        Value.createString(key)
    }

    // endregion
}
