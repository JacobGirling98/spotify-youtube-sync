package org.example.domain.model

import arrow.core.Either
import arrow.core.raise.either
import org.example.domain.error.MergeError
import org.example.util.combine

data class SongEntry(
    val song: Song,
    val serviceIds: ServiceIds
)

data class SongDictionary(
    val entries: Map<CanonicalSongKey, SongEntry> = emptyMap()
) {
    constructor(vararg pairs: Pair<Song, ServiceIds>) : this(
        pairs.associate { (song, serviceIds) ->
            song.toCanonicalKey() to SongEntry(song, serviceIds)
        }
    )

    fun mergeWith(other: SongDictionary): Either<MergeError, SongDictionary> = either {
        SongDictionary(entries.combine(other.entries) { _, sourceEntry, otherEntry ->
            val mergedIds = sourceEntry.serviceIds.mergeWith(otherEntry.serviceIds)
                .mapLeft { MergeError("Error when combining ${sourceEntry.song.name.value}: ${it.message}") }
                .bind()
            SongEntry(sourceEntry.song, mergedIds)
        })
    }

    fun ids(song: Song): ServiceIds? = entries[song.toCanonicalKey()]?.serviceIds

    companion object {
        fun empty() = SongDictionary(emptyMap())
    }
}