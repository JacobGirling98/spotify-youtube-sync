package unit.repository

import arrow.core.getOrElse
import org.example.config.CustomJackson
import org.example.domain.model.*
import org.example.domain.model.Service
import org.example.repository.JsonFileRepository
import org.example.repository.playlistRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.Pair
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaylistRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var testFile: File
    private lateinit var repository: JsonFileRepository<List<Playlist>>

    @BeforeEach
    fun setUp() {
        testFile = File(tempDir, "test_playlists.json")
        repository = playlistRepository(testFile)
    }

    @AfterEach
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun `should serialize and deserialize List of Playlist correctly`() {
        // Given
        val song1 = Song(
            name = Name("Song 1"),
            artists = listOf(Artist("Artist 1"))
        )
        val song2 = Song(
            name = Name("Song 2"),
            artists = listOf(Artist("Artist 2"))
        )

        val serviceIds1 = ServiceIds(
            Service.SPOTIFY to Id("spotifyTrackId1"),
            Service.YOUTUBE_MUSIC to Id("youtubeVideoId1")
        )
        val serviceIds2 = ServiceIds(
            Service.SPOTIFY to Id("spotifyTrackId2"),
            Service.YOUTUBE_MUSIC to Id("youtubeVideoId2")
        )

        val songDictionary1 = SongDictionary(song1 to serviceIds1)
        val songDictionary2 = SongDictionary(song2 to serviceIds2)

        val playlist1 = Playlist(
            id = Id("playlistId1"),
            name = Name("Test Playlist 1"),
            songs = songDictionary1
        )
        val playlist2 = Playlist(
            id = Id("playlistId2"),
            name = Name("Test Playlist 2"),
            songs = songDictionary2
        )

        val playlists = listOf(playlist1, playlist2)

        // When
        val saveResult = repository.save(playlists)
        assertTrue(saveResult.isRight(), "Save operation failed: ${saveResult.leftOrNull()}")

        val loadedPlaylists = repository.load().getOrElse {
            throw AssertionError("Failed to load playlists: ${it.message}")
        }

        // Then
        assertEquals(playlists, loadedPlaylists)

        // Verify the content of the file
        val fileContent = testFile.readText()
        val expectedJson = CustomJackson.asFormatString(playlists)
        assertEquals(expectedJson, fileContent)
    }

    @Test
    fun `should handle empty List of Playlist`() {
        // Given
        val emptyPlaylists = emptyList<Playlist>()

        // When
        val saveResult = repository.save(emptyPlaylists)
        assertTrue(saveResult.isRight(), "Save operation failed: ${saveResult.leftOrNull()}")

        val loadedPlaylists = repository.load().getOrElse {
            throw AssertionError("Failed to load empty playlists: ${it.message}")
        }

        // Then
        assertEquals(emptyPlaylists, loadedPlaylists)
        val fileContent = testFile.readText()
        assertEquals(CustomJackson.asFormatString(emptyPlaylists), fileContent)
    }
}
