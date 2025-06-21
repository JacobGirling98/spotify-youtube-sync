package org.example.domain.model

data class SongDictionary(
    val entries: Map<Song, Map<Service, Id>>
) {
    constructor(vararg pairs: Pair<Song, Map<Service, Id>>) : this(mapOf(*pairs))
}