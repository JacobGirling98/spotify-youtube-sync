package org.example.http.server

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.queries

fun spotifyRedirectHandler(onRequest: (String) -> Unit): HttpHandler = { request ->
    val query = request.uri.queries()
    println(query)
    Response(Status.OK)
}