package org.example.http.server

import org.example.http.auth.AuthCode
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.queries


fun redirectHandler(onRequest: (AuthCode) -> Unit): HttpHandler = { request ->
    val query = request.uri.queries()
    val code = query.firstOrNull { (first, _) -> first.lowercase() == "code" }?.second
    if (code == null) {
        Response(Status.BAD_REQUEST).body("Code was not found in request header")
    } else {
        onRequest(AuthCode(code))
        Response(Status.OK)
    }
}