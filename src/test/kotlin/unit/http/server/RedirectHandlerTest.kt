package unit.http.server

import io.kotest.matchers.shouldBe
import org.example.http.server.redirectHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.shouldHaveStatus
import kotlin.test.Test


class RedirectHandlerTest {

    @Test
    fun `can extract code from request uri`() {
        val request = Request(Method.GET, "").query("code", "auth-code")
        var extractedCode = ""
        val callback: (String) -> Unit = { extractedCode = it }

        val response = redirectHandler(callback)(request)

        extractedCode shouldBe "auth-code"
        response shouldHaveStatus OK
    }
}