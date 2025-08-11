package org.example.http.youtube.model

import org.example.domain.model.Name

data class Id(
    val videoId: org.example.domain.model.Id
)

data class Search(
    val id: Id,
    val snippet: SearchSnippet
)

data class SearchSnippet(
    val title: Name,
    val channelTitle: Name
)