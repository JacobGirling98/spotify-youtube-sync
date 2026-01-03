package org.example.domain.music

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.fold
import arrow.core.raise.either
import org.example.domain.error.Error
import org.example.domain.error.MergeError
import org.example.domain.model.*

import org.example.domain.error.NoResultsError

fun List<Playlist>.createDictionary(): Either<MergeError, SongDictionary> = either {
    fold(SongDictionary.empty()) { acc, playlist -> acc.mergeWith(playlist.songs).bind() }
}

fun SongDictionary.fillDictionary(source: Service, target: MusicService): ErrorWrapper<SongDictionary> =
    entries.fold(SongDictionary.empty().withNoErrors()) { wrapper, (_, entry) ->
        val (song, serviceIds) = entry
        if (source !in serviceIds.services || target.service in serviceIds.services) {
            wrapper.value.mergeWith(SongDictionary(song to serviceIds))
                .accumulateWith(wrapper)
        } else {
            val currentEntryDictionary = SongDictionary(song to serviceIds)
            target.search(song)
                .flatMap { candidates ->
                    val bestMatch = SongMatcher.findBestMatch(song, candidates)
                    if (bestMatch != null) {
                        currentEntryDictionary.mergeWith(SongDictionary(song to ServiceIds(target.service to bestMatch.id)))
                    } else {
                        Either.Left(NoResultsError(song))
                    }
                }
                .fold(
                    { searchOrMergeError -> ErrorWrapper(listOf(searchOrMergeError), currentEntryDictionary) },
                    { mergedDictionary -> mergedDictionary.withNoErrors() }
                )
                .flatMap { mergedDictionary -> wrapper.value.mergeWith(mergedDictionary) }
        }
    }

fun SongDictionary.subsetOf(other: SongDictionary): SongDictionary = SongDictionary(other.entries.filterKeys { key -> key in entries.keys })

private fun <T> Either<Error, T>.accumulateWith(wrapper: ErrorWrapper<T>): ErrorWrapper<T> = fold(
    { ErrorWrapper(wrapper.errors + it, wrapper.value) },
    { ErrorWrapper(wrapper.errors, it) }
)