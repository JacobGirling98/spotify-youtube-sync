package org.example.domain.music

import org.example.domain.model.Song
import org.example.domain.model.SongMatchCandidate

object SongMatcher {
    fun findBestMatch(original: Song, candidates: List<SongMatchCandidate>): SongMatchCandidate? {
        val cleanedOriginalTitle = cleanCoreTitle(original.name.value)
        val originalVersionTags = extractVersionTags(original.name.value)
        val originalArtists = original.artists.map { it.value.lowercase() }.toSet()

        return candidates.firstOrNull { candidate ->
            val cleanedCandidateTitle = cleanCoreTitle(candidate.title)
            val candidateVersionTags = extractVersionTags(candidate.title)
            
            // Primary Title Match
            val titleMatches = cleanedOriginalTitle == cleanedCandidateTitle || 
                               (cleanedCandidateTitle.contains(cleanedOriginalTitle) && cleanedOriginalTitle.isNotBlank()) ||
                               (cleanedOriginalTitle.contains(cleanedCandidateTitle) && cleanedCandidateTitle.isNotBlank())


            if (!titleMatches) return@firstOrNull false

            // Artist Match
            val candidateText = (candidate.channelTitle + " " + candidate.title).lowercase()
            val artistMatches = originalArtists.any { artist -> candidateText.contains(artist) }

            if (!artistMatches) return@firstOrNull false

            // Version Tag Match - this is crucial for distinguishing
            originalVersionTags == candidateVersionTags
        }
    }

    internal fun cleanCoreTitle(title: String): String {
        var text = title.lowercase()

        // Remove truly irrelevant noise for core title comparison, including featured artists from title for core match
        val noisePatterns = listOf(
            "\\(feat.*?\\)", // (feat. X)
            "\\(with.*?\\)", // (with X)
            "\\[.*?\\]", // [Official Video], [Lyrics]
            "\\(official.*?\\)", // (Official Audio)
            " - topic", // YouTube Topic channels suffix
            "lyrics",
            "official video",
            "official audio",
            "mv" // Music video tag
        )

        noisePatterns.forEach { pattern ->
            text = text.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove special characters, keep alphanumeric and spaces
        text = text.replace("'", "") // Remove apostrophes here for core matching
        text = text.replace(Regex("[^a-z0-9 ]"), "")

        return text.trim().replace(Regex("\\s+"), " ")
    }

    internal fun extractVersionTags(title: String): Set<String> {
        val tags = mutableSetOf<String>()
        val lowerTitle = title.lowercase()

        val versionRegexes = listOf(
            Regex("\\(remix\\)", RegexOption.IGNORE_CASE),
            Regex(" - remix", RegexOption.IGNORE_CASE),
            Regex("\\(acoustic\\)", RegexOption.IGNORE_CASE),
            Regex(" - acoustic", RegexOption.IGNORE_CASE),
            Regex("\\(live\\)", RegexOption.IGNORE_CASE),
            Regex(" - live", RegexOption.IGNORE_CASE),
            Regex("\\(taylor.s version\\)", RegexOption.IGNORE_CASE),
            Regex(" - taylor.s version", RegexOption.IGNORE_CASE),
            Regex("\\(atl.s version\\)", RegexOption.IGNORE_CASE),
            Regex(" - atl.s version", RegexOption.IGNORE_CASE),
            Regex("\\(from the room below\\)", RegexOption.IGNORE_CASE),
            Regex(" - from the room below", RegexOption.IGNORE_CASE)
            // Removed (feat. X) and (with X) from here, these are featured artists, not version tags
        )

        versionRegexes.forEach { regex ->
            regex.findAll(lowerTitle).forEach { match ->
                val tag = match.value.lowercase()
                    .replace(Regex("[^a-z0-9 ]"), "") // Remove punctuation
                    .trim()
                if (tag.isNotBlank()) {
                    tags.add(tag.replace(Regex("\\s+"), " ")) // Normalize spaces and add
                }
            }
        }
        return tags
    }
}