package org.example.domain.model

data class SongMatchCandidate(
    val id: Id,
    val title: String,
    val channelTitle: String,
    val durationMs: Long? = null // Nullable because search results might not always have details
)
