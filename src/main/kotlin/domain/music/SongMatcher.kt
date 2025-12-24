package org.example.domain.music

import org.example.domain.model.Song
import org.example.domain.model.SongMatchCandidate

object SongMatcher {
    fun findBestMatch(original: Song, candidates: List<SongMatchCandidate>): SongMatchCandidate? {
        val normalizedOriginalTitle = normalizeTitle(original.name.value)
        val originalArtists = original.artists.map { it.value.lowercase() }.toSet()

        // 1. Filter candidates that match the title (fuzzy)
        val titleMatches = candidates.filter {
            val normalizedCandidateTitle = normalizeTitle(it.title)
            // Check for exact match of normalized strings, or strict containment
            normalizedOriginalTitle == normalizedCandidateTitle || 
            normalizedCandidateTitle.contains(normalizedOriginalTitle) ||
            normalizedOriginalTitle.contains(normalizedCandidateTitle)
        }

        // 2. Filter / Select based on Artist match
        // We check if ANY of the original artists appear in the Channel Title OR the Video Title
        return titleMatches.firstOrNull {
            val candidateText = (it.channelTitle + " " + it.title).lowercase() 
            
            originalArtists.any {
                // Check if artist name is in the candidate text
                // We assume artist names are significant enough not to be random noise
                candidateText.contains(it)
            }
        }
    }

    internal fun normalizeTitle(title: String): String {
        var text = title.lowercase()

        // Remove (feat. X), (with X), [Official Video], etc.
        val patterns = listOf(
            "\\(feat.*?\\)",
            "\\(with.*?\\)",
            "\\[.*?\\]", // [Official Video]
            "\\(official.*?\\)", // (Official Audio)
            " - topic", // YouTube Topic channels suffix
            "lyrics",
            "official video",
            "official audio"
        )

        patterns.forEach {
            text = text.replace(Regex(it, RegexOption.IGNORE_CASE), "")
        }

        // Remove special characters that might differ (like hyphens, dots)
        // Keep alphanumeric and spaces
        text = text.replace(Regex("[^a-z0-9 ]"), "")

        return text.trim().replace(Regex("\\s+"), " ")
    }
}
