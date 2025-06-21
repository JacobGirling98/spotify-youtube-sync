package org.example.http.spotify.model

import org.example.domain.model.Id
import org.example.domain.model.Name

data class Album(
    val id: Id,
    val name: Name,
    val artists: List<Artist>
)
