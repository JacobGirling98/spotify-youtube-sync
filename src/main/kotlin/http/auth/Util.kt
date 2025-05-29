package org.example.http.auth

import arrow.core.Either

typealias TokenResult = Either<GetTokenError, Token>