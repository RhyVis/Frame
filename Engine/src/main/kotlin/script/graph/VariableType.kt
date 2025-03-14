package rhx.frame.script.graph

import rhx.frame.core.ScriptException
import rhx.frame.core.graph.Value

enum class VariableType {
    INT32,
    INT64,
    FLOAT,
    BOOL,
    STRING,
    OBJECT,
    ;

    fun defaultValue(): Value =
        when (this) {
            INT32 -> Value.createInt32(0)
            INT64 -> Value.createInt64(0)
            FLOAT -> Value.createFloat(0.0)
            BOOL -> Value.createBool(false)
            STRING -> Value.createString("")
            OBJECT -> throw ScriptException("Cannot create default value for OBJECT type")
        }
}
