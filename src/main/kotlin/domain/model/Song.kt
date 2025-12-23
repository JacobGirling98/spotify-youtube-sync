package org.example.domain.model

data class Song(
    val name: Name,
    val artists: List<Artist>
) {
    override fun toString(): String {
        return "${name.value} - ${artists.joinToString { it.value }}"
    }

    private val normalizedName: String
        get() {
            val lower = name.value.lowercase()
            return when {
                lower.contains(" (with") -> lower.substringBefore(" (with")
                lower.contains(" (feat") -> lower.substringBefore(" (feat")
                else -> lower
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (normalizedName != other.normalizedName) return false
        
        // Match if any artist is common to both songs
        val otherArtists = other.artists.toSet()
        if (artists.none { it in otherArtists }) return false

        return true
    }

    override fun hashCode(): Int {
        return normalizedName.hashCode()
    }
}