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

        if (name.value.lowercase() != other.name.value.lowercase()) return false
        if (artists != other.artists) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + artists.hashCode()
        return result
    }
}
