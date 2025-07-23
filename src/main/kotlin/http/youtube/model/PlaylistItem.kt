package org.example.http.youtube.model

import org.example.domain.model.Id
import org.example.domain.model.Name

data class PlaylistItem(
    val id: Id,
    val snippet: PlaylistItemSnippet
)

data class PlaylistItemSnippet(
    val title: Name,
    val videoOwnerChannelId: Id,
    val videoOwnerChannelTitle: Name
)