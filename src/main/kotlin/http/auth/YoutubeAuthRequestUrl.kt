package org.example.http.auth

import java.net.URLEncoder

fun youtubeAuthRequestUrl(clientId: String): String {
    val redirectUri: String = URLEncoder.encode("urn:ietf:wg:oauth:2.0:oob", "UTF-8")
    val scope: String = URLEncoder.encode("https://www.googleapis.com/auth/youtube.readonly", "UTF-8")

    val authorizationUrl = "https://accounts.google.com/o/oauth2/auth?" +
            "client_id=$clientId&" +
            "redirect_uri=$redirectUri&" +
            "response_type=code&" +
            "scope=$scope&" +
            "access_type=offline&" +
            "prompt=consent"

    return authorizationUrl
}
