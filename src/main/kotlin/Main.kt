package org.example

import java.net.URLEncoder

fun main() {
    val clientId = System.getenv("CLIENT_ID")
    val redirectUri = URLEncoder.encode("urn:ietf:wg:oauth:2.0:oob", "UTF-8")
    val scope = URLEncoder.encode("https://www.googleapis.com/auth/youtube.readonly", "UTF-8")

    val authorizationUrl = "https://accounts.google.com/o/oauth2/auth?" +
            "client_id=$clientId&" +
            "redirect_uri=$redirectUri&" +
            "response_type=code&" +
            "scope=$scope&" +
            "access_type=offline&" +
            "prompt=consent"

    println("Please open the following URL in your browser:\n$authorizationUrl")
}