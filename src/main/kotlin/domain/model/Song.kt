package org.example.domain.model

data class Song(
    val name: Name,
    val artists: List<Artist>
) {
    override fun toString(): String {
        return "${name.value} - ${artists.joinToString { it.value }}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (name != other.name) return false
        // Fuzzy match: match if the first artist is the same
        val thisFirstArtist = artists.firstOrNull()
        val otherFirstArtist = other.artists.firstOrNull()
        if (thisFirstArtist != otherFirstArtist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (artists.firstOrNull()?.hashCode() ?: 0)
        return result
    }
}