package org.example.http.youtube.model

data class Page<T>(
    val nextPageToken: String?,
    val items: List<T>
)
