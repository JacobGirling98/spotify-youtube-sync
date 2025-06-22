package org.example.http.auth

import arrow.core.Either
import org.example.domain.error.HttpError

typealias TokenResult = Either<HttpError, Token>