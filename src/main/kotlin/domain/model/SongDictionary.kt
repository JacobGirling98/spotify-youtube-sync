package org.example.domain.model

data class ServiceIds(
    val entries: Map<Service, Id>
) {
    constructor(vararg pairs: Pair<Service, Id>) : this(mapOf(*pairs))
}

data class SongDictionary(
    val entries: Map<Song, ServiceIds>
) {
    constructor(vararg pairs: Pair<Song, ServiceIds>) : this(mapOf(*pairs))
}