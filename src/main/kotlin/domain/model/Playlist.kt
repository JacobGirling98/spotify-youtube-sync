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
        val added = other.songs.entries.keys.filter { song -> song !in songs.entries.keys }
        val removed = songs.entries.keys.filter { song -> song !in other.songs.entries.keys }
        return Delta(added, removed)
    }
}
