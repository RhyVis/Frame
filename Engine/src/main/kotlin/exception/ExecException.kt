package rhx.frame.exception

open class GraphExecException(
    message: String,
    cause: Throwable? = null,
) : EngineException(message, cause)

class JumpCalledException(
    val targetId: String,
) : GraphExecException("Jump called")

class JumpTargetNotFoundException(
    targetId: String,
) : GraphExecException("Jump target $targetId not found")

class JumpLimitExceededException(
    message: String,
) : GraphExecException(message)

class LoopLimitExceededException(
    message: String,
) : GraphExecException(message)

class ScriptException(
    message: String,
    cause: Throwable? = null,
) : GraphExecException(message, cause)
