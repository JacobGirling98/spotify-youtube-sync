package org.example.domain.model

import org.example.domain.music.SongMatcher

data class Song(
    val name: Name,
    val artists: List<Artist>
) {
    override fun toString(): String {
        return "${name.value} - ${artists.joinToString { it.value }}"
    }

    fun toCanonicalKey(): CanonicalSongKey {
        val cleanedTitle = SongMatcher.cleanTitleForCanonicalKey(this.name.value)
        val artistsKey = this.artists.map { it.value.lowercase() }.sorted().joinToString(",")
        
        return CanonicalSongKey("$cleanedTitle::$artistsKey")
    }
}