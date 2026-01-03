package org.example.domain.model

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
        val added = other.songs.entries.filterKeys { it !in songs.entries.keys }.values.map { it.song }
        val removed = songs.entries.filterKeys { it !in other.songs.entries.keys }.values.map { it.song }
        return Delta(added, removed)
    }
}
