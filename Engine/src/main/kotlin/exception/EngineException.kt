package rhx.frame.exception

abstract class EngineException : FrameRuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)
}

class RuntimeIncompleteException(
    message: String,
) : EngineException(message)

class GraphParsingException(
    message: String,
    cause: Throwable?,
) : EngineException(message, cause)
