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

data class PlaylistItemRequest(
    val snippet: PlaylistItemSnippetRequest
)

data class PlaylistItemSnippetRequest(
    val playlistId: Id,
    val resourceId: ResourceId
)

data class ResourceId(
    val videoId: Id
)