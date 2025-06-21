package org.example.util

import arrow.core.Either
import org.example.http.auth.JsonError

fun <T> catchJsonError(block: () -> T): Either<JsonError, T> =
    Either.Companion.catch(block).mapLeft { JsonError(it.message) }