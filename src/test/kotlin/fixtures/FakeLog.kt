package fixtures

import org.example.log.Log

class FakeLog : Log {
    val messages = mutableListOf<String>()

    override fun debug(message: String) {
        messages.add("DEBUG: $message")
    }

    override fun info(message: String) {
        messages.add("INFO: $message")
    }

    override fun warn(message: String) {
        messages.add("WARN: $message")
    }

    override fun error(message: String, throwable: Throwable?) {
        messages.add("ERROR: $message")
    }
}
