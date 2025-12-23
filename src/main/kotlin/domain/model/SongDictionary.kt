package org.example.domain.model

import arrow.core.Either
import arrow.core.raise.either
import org.example.domain.error.MergeError
import org.example.util.combine

data class SongDictionary(
    val entries: Map<Song, ServiceIds> = emptyMap()
) {
    constructor(vararg pairs: Pair<Song, ServiceIds>) : this(
        if (pairs.isEmpty()) emptyMap() else mapOf(*pairs)
    )

    fun mergeWith(other: SongDictionary): Either<MergeError, SongDictionary> = either {
        SongDictionary(entries.combine(other.entries) { song, sourceServiceIds, otherServiceIds ->
            sourceServiceIds.mergeWith(otherServiceIds).mapLeft { MergeError("Error when combining ${song.name.value}: ${it.message}") }
                .bind()
        })
    }

    fun ids(song: Song): ServiceIds? = entries[song]

    companion object {
        fun empty() = SongDictionary(emptyMap())
    }
}