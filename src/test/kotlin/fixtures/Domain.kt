package fixtures

import org.example.domain.model.*
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC

fun artist(name: String = "David") = Artist(name)
fun artists(name: String = "David") = listOf(artist())
fun song(name: String = "Free Love Freeway", artists: List<Artist> = artists()) = Song(Name(name), artists())
fun serviceIds(service: Service = SPOTIFY, id: Id = Id("123")) = ServiceIds(service to id)
fun spotifyServiceId(id: Id = Id("123")) = serviceIds(SPOTIFY, id)
fun youtubeServiceId(id: Id = Id("123")) = serviceIds(YOUTUBE_MUSIC, id)
fun songDictionary(song: Song = song(), serviceIds: ServiceIds = serviceIds()) =
    SongDictionary(song to serviceIds)

fun playlist(name: String = "David Brent's Playlist", songDictionary: SongDictionary = songDictionary()) =
    Playlist(Name(name), songDictionary)