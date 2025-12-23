package org.example.domain.model

data class Song(
    val name: Name,
    val artists: List<Artist>
) {
    override fun toString(): String {
        return "${name.value} - ${artists.joinToString { it.value }}"
    }
}