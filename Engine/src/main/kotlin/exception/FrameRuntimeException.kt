package rhx.frame.exception

abstract class FrameRuntimeException : RuntimeException {
    constructor() : super()
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable? = null) : super(message, cause)
}
