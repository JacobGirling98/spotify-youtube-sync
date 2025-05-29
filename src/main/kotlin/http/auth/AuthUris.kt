package org.example.http.auth

import java.net.URLEncoder

interface AuthUris {
    val localServiceUri: String
    val internalRedirectRoute: String
    val baseCodeUri: String
    val tokenUri: String
    val scope: String

    val redirectUri: String
        get() = "$localServiceUri/$internalRedirectRoute"

    fun codeUri(clientId: String, additionalParams: String? = null): String {
        val scope = URLEncoder.encode(scope, "UTF-8")

        val authorizationUrl = buildString {
            append("${baseCodeUri}?")
            append("response_type=code&")
            append("client_id=$clientId&")
            append("scope=$scope&")
            append("redirect_uri=$localServiceUri/$internalRedirectRoute")
            if (additionalParams != null) append("&$additionalParams")
        }

        return authorizationUrl
    }
}

data class SpotifyAuth(override val localServiceUri: String) : AuthUris {
    override val internalRedirectRoute: String = "spotify_callback"
    override val baseCodeUri: String = "https://accounts.spotify.com/authorize"
    override val tokenUri: String = "https://accounts.spotify.com/api/token"
    override val scope: String = "playlist-read-private playlist-read-collaborative"

}

data class YouTubeAuth(override val localServiceUri: String) : AuthUris {
    override val internalRedirectRoute: String = "youtube_callback"
    override val baseCodeUri: String = "https://accounts.google.com/o/oauth2/auth"
    override val tokenUri: String = "https://oauth2.googleapis.com/token"
    override val scope: String = "https://www.googleapis.com/auth/youtube.readonly"

    fun codeUri(clientId: String) = codeUri(clientId, "access_type=offline&prompt=consent")
}