package rhx.frame.core.graph

import java.util.Scanner

@Suppress("MemberVisibilityCanBePrivate")
object SystemEnv {
    // region System functions

    private val except = { args: List<Value> ->
        throw ScriptException(args.joinToString(" ") { it.toString() })
    }

    private val toStringSys = { args: List<Value> ->
        Value.createString(args[0].toString())
    }

    private val print = { args: List<Value> ->
        print(args.joinToString(" ") { it.toString() })
        Value.VOID
    }

    private val printLn = { args: List<Value> ->
        println(args.joinToString(" ") { it.toString() })
        Value.VOID
    }

    private val printFormat = { args: List<Value> ->
        val format = args[0].toString()
        val values = args.drop(1).map { it.toString() }.toTypedArray()
        println(format.format(*values))
        Value.VOID
    }

    private val waitInput = { args: List<Value> ->
        val message = if (args.isNotEmpty()) args[0].toString() else "Press any key to continue..."
        println(message)
        val key =
            Scanner(System.`in`).use {
                it.nextLine()
            }
        Value.createString(key.toString())
    }

    // endregion

    /**
     * System function map
     *
     * @see systemCall
     */
    val SystemCallMap =
        mutableMapOf(
            "except" to except,
            "to_string" to toStringSys,
            "print" to print,
            "println" to printLn,
            "printf" to printFormat,
            "wait_input" to waitInput,
        )

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
            SystemCallMap[name]
                ?: throw IllegalArgumentException("System function $name not found")

        return func(args)
    }

    /**
     * Register system function
     *
     * @param name function name
     * @param func function implementation
     */
    fun registerSystemCall(
        name: String,
        func: (List<Value>) -> Value,
    ) {
        SystemCallMap[name] = func
    }
}
