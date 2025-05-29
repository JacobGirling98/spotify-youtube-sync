package org.example.http.server

import org.example.http.auth.AuthCode
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.queries

// TODO - make more robust
// TODO - add a body to let me know it worked correctly
fun redirectHandler(onRequest: (AuthCode) -> Unit): HttpHandler = { request ->
    val query = request.uri.queries()
    val code = query.firstOrNull { (first, _) -> first.lowercase() == "code" }?.second ?: "not a code!"
    onRequest(AuthCode(code))
    Response(Status.OK)
}