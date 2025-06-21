package org.example.http.spotify.model

data class Page<T>(
    val next: String?,
    val items: List<T>
)
