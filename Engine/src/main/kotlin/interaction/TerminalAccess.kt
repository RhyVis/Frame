package rhx.frame.interaction

class TerminalAccess : Access {
    override fun display(text: String) {
        print(text)
    }

    override fun displayLn(text: String) {
        println(text)
    }

    override fun waitForInput(): String = readln()
}
