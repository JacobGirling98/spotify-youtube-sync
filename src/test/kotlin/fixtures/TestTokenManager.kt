package fixtures

import arrow.core.Either
import org.example.domain.error.HttpError
import org.example.domain.error.HttpResponseError
import org.example.http.auth.AccessToken
import org.example.http.auth.AuthCode
import org.example.http.auth.TokenManager

class TestTokenManager(
    private val tokenToReturn: String = "token",
    private val tokenFailure: Boolean = false
) : TokenManager {
    override fun token(): Either<HttpError, AccessToken> {
        if (tokenFailure)
            return Either.Left(HttpResponseError(400, "oops"))
        return Either.Right(AccessToken(tokenToReturn))
    }

    override fun updateAuthCode(authCode: AuthCode) {
        TODO("Not yet implemented")
    }
}