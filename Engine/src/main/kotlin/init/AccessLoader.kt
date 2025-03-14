package rhx.frame.init

import rhx.frame.interaction.Access
import rhx.frame.interaction.TerminalAccess

object AccessLoader {
    fun load() {
        Access.instance = TerminalAccess()
    }
}
