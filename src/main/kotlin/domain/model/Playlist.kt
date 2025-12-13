package org.example.domain.model

data class Playlist(
    val id: Id,
    val name: Name,
    val songs: SongDictionary
)
