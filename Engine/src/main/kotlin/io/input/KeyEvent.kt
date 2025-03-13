package rhx.frame.io.input

data class KeyEvent(
    val type: KeyEventType,
    val keyChar: Char,
    val keyCode: Int = keyChar.code,
)
