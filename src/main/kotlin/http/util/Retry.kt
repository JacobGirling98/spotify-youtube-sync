package org.example.http.util

import org.http4k.core.HttpHandler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


fun retry(
    handler: HttpHandler,
    max: Int = 3,
    delay: Duration = 100.milliseconds,
    sleep: (Duration) -> Unit = { Thread.sleep(it.inWholeMilliseconds) }
): HttpHandler = { request ->
    var retries = 0
    var response = handler(request)
    while (retries < max && !response.status.successful) {
        sleep(delay)
        response = handler(request)
        retries++
    }
    response
}