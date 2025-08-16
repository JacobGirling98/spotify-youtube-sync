package org.example.domain.model

import arrow.core.Either
import org.example.domain.error.Error

data class ErrorWrapper<T>(
    val errors: List<Error>,
    val value: T
)

fun <T> T.withNoErrors() = ErrorWrapper<T>(emptyList(), this)

fun <T> ErrorWrapper<T>.flatMap(fn: (T) -> Either<Error, T>): ErrorWrapper<T> = fn(this.value).fold(
    { ErrorWrapper(errors + it, value) },
    { ErrorWrapper(errors, it) }
)
