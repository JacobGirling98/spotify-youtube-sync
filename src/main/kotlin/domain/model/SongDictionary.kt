package org.example.domain.model

import arrow.core.Either
import arrow.core.raise.either
import org.example.domain.error.MergeError
import org.example.domain.music.SongMatcher
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
        val resultMap = entries.toMutableMap()
        
        other.entries.forEach { (key, otherEntry) ->
            if (resultMap.containsKey(key)) {
                // Exact Match
                val sourceEntry = resultMap[key]!!
                val mergedIds = sourceEntry.serviceIds.mergeWith(otherEntry.serviceIds)
                    .mapLeft { MergeError("Error when combining ${sourceEntry.song.name.value}: ${it.message}") }
                    .bind()
                resultMap[key] = SongEntry(sourceEntry.song, mergedIds)
            } else {
                // Fuzzy Match
                val candidate = otherEntry.song.toMatchCandidate()
                val match = resultMap.values.firstOrNull { sourceEntry ->
                    SongMatcher.matches(sourceEntry.song, candidate)
                }

                if (match != null) {
                    val mergedIds = match.serviceIds.mergeWith(otherEntry.serviceIds)
                        .mapLeft { MergeError("Error when combining ${match.song.name.value}: ${it.message}") }
                        .bind()
                    // Update the existing entry with merged IDs, preserving the original Song key/value
                    resultMap[match.song.toCanonicalKey()] = SongEntry(match.song, mergedIds)
                } else {
                    // No Match
                    resultMap[key] = otherEntry
                }
            }
        }
        
        SongDictionary(resultMap)
    }

    fun ids(song: Song): ServiceIds? = entries[song.toCanonicalKey()]?.serviceIds

    companion object {
        fun empty() = SongDictionary(emptyMap())
    }
}