package org.example.domain.model

import arrow.core.Either
import arrow.core.raise.either
import org.example.domain.error.MergeError
import org.example.util.combine

data class SongDictionary(
    val entries: Map<Song, ServiceIds>
) {
    constructor(vararg pairs: Pair<Song, ServiceIds>) : this(mapOf(*pairs))

    fun mergeWith(other: SongDictionary): Either<MergeError, SongDictionary> = either {
        SongDictionary(entries.combine(other.entries) { song, first, second ->
            first.mergeWith(second).mapLeft { MergeError("Error when combining ${song.name.value}: ${it.message}") }
                .bind()
        })
    }

    companion object {
        fun empty() = SongDictionary()
    }
}