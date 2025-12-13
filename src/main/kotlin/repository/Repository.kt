package org.example.repository

import arrow.core.Either
import org.example.domain.error.Error

interface Repository<T> {
    fun load(): Either<Error, T>
    fun save(data: T): Either<Error, Unit>
}