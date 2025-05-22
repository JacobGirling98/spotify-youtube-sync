package org.example.http.auth

import java.net.URLEncoder

fun spotifyAuthRequestUrl(clientId: String): String {
    val redirectUri = "http://127.0.0.1:8000/spotify_callback"
    val scope = URLEncoder.encode("playlist-read-private playlist-read-collaborative", "UTF-8")

    val authorizationUrl = "https://accounts.spotify.com/authorize?" +
            "response_type=code&" +
            "client_id=$clientId&" +
            "scope=$scope&" +
            "redirect_uri=$redirectUri"

    return authorizationUrl
}