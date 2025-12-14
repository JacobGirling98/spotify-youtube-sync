package unit.repository

import arrow.core.getOrElse
import org.example.config.CustomJackson
import org.example.domain.model.*
import org.example.repository.JsonFileRepository
import org.example.repository.songDictionaryRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SongDictionaryRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var testFile: File
    private lateinit var repository: JsonFileRepository<SongDictionary>

    @BeforeEach
    fun setUp() {
        testFile = File(tempDir, "test_song_dictionary.json")
        repository = songDictionaryRepository(testFile)
    }

    @AfterEach
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun `should serialize and deserialize SongDictionary correctly`() {
        // Given
        val song = Song(
            name = Name("Test Song"),
            artists = listOf(Artist("Test Artist"))
        )
        val serviceIds = ServiceIds(
            kotlin.Pair(Service.SPOTIFY, Id("spotifyTrackId")),
            kotlin.Pair(Service.YOUTUBE_MUSIC, Id("youtubeVideoId"))
        )
        val songDictionary = SongDictionary(
            song to serviceIds
        )

        // When
        val saveResult = repository.save(songDictionary)
        assertTrue(saveResult.isRight(), "Save operation failed: ${saveResult.leftOrNull()}")

        val loadedSongDictionary = repository.load().getOrElse {
            throw AssertionError("Failed to load song dictionary: ${it.message}")
        }

        // Then
        assertEquals(songDictionary, loadedSongDictionary)

        // Verify the content of the file
        val fileContent = testFile.readText()
        val expectedJson = CustomJackson.asFormatString(songDictionary)
        assertEquals(expectedJson, fileContent)
    }

    @Test
    fun `should handle empty SongDictionary`() {
        // Given
        val emptySongDictionary = SongDictionary(emptyMap())

        // When
        val saveResult = repository.save(emptySongDictionary)
        assertTrue(saveResult.isRight(), "Save operation failed: ${saveResult.leftOrNull()}")

        val loadedSongDictionary = repository.load().getOrElse {
            throw AssertionError("Failed to load empty song dictionary: ${it.message}")
        }

        // Then
        assertEquals(emptySongDictionary, loadedSongDictionary)
        val fileContent = testFile.readText()
        assertEquals(CustomJackson.asFormatString(emptySongDictionary), fileContent)
    }
}
