package org.example.domain.model

import org.example.domain.music.SongMatcher

data class Delta(
    val added: List<Song> = emptyList(),
    val removed: List<Song> = emptyList()
) {
    companion object {
        fun empty() = Delta(emptyList(), emptyList())
    }
}

data class Playlist(
    val id: Id,
    val name: Name,
    val songs: SongDictionary
) {
    fun deltaWith(other: Playlist): Delta {
        val added = other.songs.entries.values.filter { otherEntry ->
            // Check if this song exists in 'this' playlist (exact or fuzzy)
            val exactMatch = songs.entries.containsKey(otherEntry.song.toCanonicalKey())
            if (exactMatch) return@filter false

            val candidate = otherEntry.song.toMatchCandidate()
            val fuzzyMatch = songs.entries.values.any { myEntry ->
                SongMatcher.matches(myEntry.song, candidate)
            }
            !fuzzyMatch
        }.map { it.song }

        val removed = songs.entries.values.filter { myEntry ->
            // Check if this song exists in 'other' playlist (exact or fuzzy)
            val exactMatch = other.songs.entries.containsKey(myEntry.song.toCanonicalKey())
            if (exactMatch) return@filter false

            val candidate = myEntry.song.toMatchCandidate()
            val fuzzyMatch = other.songs.entries.values.any { otherEntry ->
                SongMatcher.matches(otherEntry.song, candidate)
            }
            !fuzzyMatch
        }.map { it.song }

        return Delta(added, removed)
    }
}
