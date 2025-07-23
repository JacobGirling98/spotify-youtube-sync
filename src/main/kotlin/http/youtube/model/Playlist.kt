package org.example.http.youtube.model

import org.example.domain.model.Id
import org.example.domain.model.Name

data class Playlist(val id: Id, val snippet: PlaylistSnippet)

data class PlaylistSnippet(val title: Name)