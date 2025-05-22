package unit.http.util

import io.kotest.matchers.shouldBe
import org.example.http.util.retry
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.shouldHaveStatus
import kotlin.test.Test
import kotlin.time.Duration

class RetryTest {

    private val stubSleep: (Duration) -> Unit = {}

    @Test
    fun `does not retry on success`() {
        val (handler, stats) = stubHandler(failures = 0)

        val response = retry(handler, sleep = stubSleep)(
            Request(Method.GET, "/")
        )

        response shouldHaveStatus OK
        stats.calls shouldBe 1
        stats.errors shouldBe 0
    }

    @Test
    fun `retries on a failure`() {
        val (handler, stats) = stubHandler(failures = 1)

        val response = retry(handler, sleep = stubSleep)(
            Request(Method.GET, "/")
        )

        response shouldHaveStatus OK
        stats.calls shouldBe 2
        stats.errors shouldBe 1
    }

    @Test
    fun `retries up to max retries`() {
        val (handler, stats) = stubHandler(failures = 10)

        val response = retry(handler, max = 5, sleep = stubSleep)(
            Request(Method.GET, "/")
        )

        response shouldHaveStatus BAD_REQUEST
        stats.calls shouldBe 6
        stats.errors shouldBe 6
    }

    @Test
    fun `sleeps in between retries`() {
        val (handler) = stubHandler(failures = 10)
        var sleepCount = 0
        val retries = 5
        val sleep: (Duration) -> Unit = {sleepCount++}

        retry(handler, max = retries, sleep = sleep)(
            Request(Method.GET, "/")
        )

        sleepCount shouldBe retries
    }

    private fun stubHandler(failures: Int): Pair<HttpHandler, Stats> {
        val stats = Stats(0, 0)

        val handler: HttpHandler = {
            stats.calls++
            if (stats.calls <= failures) {
                stats.errors++
                Response(BAD_REQUEST)
            } else {
                Response(OK)
            }

        }

        return Pair(handler, stats)
    }

    private data class Stats(
        var calls: Int,
        var errors: Int
    )
}