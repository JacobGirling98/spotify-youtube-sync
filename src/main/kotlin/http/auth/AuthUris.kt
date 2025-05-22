package org.example.http.auth

import java.net.URLEncoder

interface AuthUris {
    val redirectUri: String
    val baseCodeUri: String
    val tokenUri: String
    val scope: String

    fun codeUri(clientId: String, additionalParams: String? = null): String {
        val redirectUri = "http://127.0.0.1:8000/spotify_callback"
        val scope = URLEncoder.encode(scope, "UTF-8")

        val authorizationUrl = buildString {
            append("${baseCodeUri}?")
            append("response_type=code&")
            append("client_id=$clientId&")
            append("scope=$scope&")
            append("redirect_uri=$redirectUri")
            if (additionalParams != null) append("&$additionalParams")
        }

        return authorizationUrl
    }
}

object SpotifyAuth : AuthUris {
    override val redirectUri: String = "http://127.0.0.1:8000/spotify_callback"
    override val baseCodeUri: String = "https://accounts.spotify.com/authorize"
    override val tokenUri: String = "https://accounts.spotify.com/api/token"
    override val scope: String = "playlist-read-private playlist-read-collaborative"
}

object YouTubeAuth : AuthUris {
    override val redirectUri: String = "urn:ietf:wg:oauth:2.0:oob"
    override val baseCodeUri: String = "https://accounts.google.com/o/oauth2/auth"
    override val tokenUri: String = ""
    override val scope: String = "https://www.googleapis.com/auth/youtube.readonly"

    fun codeUri(clientId: String) = codeUri(clientId, "access_type=offline&prompt=consent")
}