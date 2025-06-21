package org.example.http.util

import arrow.core.Either
import org.example.http.auth.HttpResponseError
import org.http4k.core.Response

fun httpResponseError(response: Response): Either.Left<HttpResponseError> =
    Either.Left(HttpResponseError.from(response))