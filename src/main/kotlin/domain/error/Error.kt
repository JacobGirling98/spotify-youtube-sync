package org.example.domain.error

import org.example.domain.model.Song
import org.http4k.core.Response

sealed class Error(open val message: String?)

sealed class HttpError(override val message: String?) : Error(message)
data class HttpResponseError(val statusCode: Int, override val message: String) : HttpError(message) {
    companion object {
        fun from(response: Response) = HttpResponseError(response.status.code, response.bodyString())
    }
}

sealed class GetTokenError(override val message: String?) : HttpError(message)
data class JsonError(override val message: String?) : GetTokenError(message)
data object TokenNotSet : GetTokenError("Token has not been set")
data object AuthCodeNotSet : GetTokenError("Auth code has not been set")

data class NoResultsError(val song: Song) : HttpError("No search results for song: ${song.name.value}")

data class MergeError(override val message: String) : Error(message)