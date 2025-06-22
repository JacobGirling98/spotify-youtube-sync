package org.example.domain.model

import arrow.core.Either
import arrow.core.combine
import arrow.core.raise.either
import org.example.domain.error.MergeError

data class SongDictionary(
    val entries: Map<Song, ServiceIds>
) {
    constructor(vararg pairs: Pair<Song, ServiceIds>) : this(mapOf(*pairs))

    fun mergeWith(other: SongDictionary): Either<MergeError, SongDictionary> = either {
        SongDictionary(entries.combine(other.entries) { first, second ->
            first.mergeWith(second).bind()
        })
    }
}