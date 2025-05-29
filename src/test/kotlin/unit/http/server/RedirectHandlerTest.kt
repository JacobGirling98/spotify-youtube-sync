package unit.http.server

import io.kotest.matchers.shouldBe
import org.example.http.auth.AuthCode
import org.example.http.server.redirectHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import kotlin.test.Test


class RedirectHandlerTest {

    @Test
    fun `can extract code from request uri`() {
        val request = Request(Method.GET, "").query("code", "auth-code")
        var extractedCode = ""
        val callback: (AuthCode) -> Unit = { extractedCode = it.value }

        val response = redirectHandler(callback)(request)

        extractedCode shouldBe "auth-code"
        response shouldHaveStatus OK
    }

    @Test
    fun `fails if code is not in the header`() {
        val request = Request(Method.GET, "")
        val unitCallback: (AuthCode) -> Unit = {  }

        val response = redirectHandler(unitCallback)(request)

        response shouldHaveStatus BAD_REQUEST
        response shouldHaveBody "Code was not found in request header"
    }
}