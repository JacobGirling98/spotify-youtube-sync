package org.example.http.youtube.model

import org.example.domain.model.Id
import org.example.domain.model.Name

data class Playlist(val id: Id, val snippet: Snippet)

data class Snippet(val title: Name)