package fixturesTests

import io.kotest.matchers.collections.shouldContainExactly
import fixtures.FakeLog
import org.junit.jupiter.api.Test

class FakeLogTest {

    @Test
    fun `should collect logs for all levels`() {
        val fakeLog = FakeLog()

        fakeLog.debug("debug message")
        fakeLog.info("info message")
        fakeLog.warn("warn message")
        fakeLog.error("error message")
        fakeLog.error("error with throwable", RuntimeException("oops"))

        fakeLog.messages shouldContainExactly listOf(
            "DEBUG: debug message",
            "INFO: info message",
            "WARN: warn message",
            "ERROR: error message",
            "ERROR: error with throwable"
        )
    }
}
