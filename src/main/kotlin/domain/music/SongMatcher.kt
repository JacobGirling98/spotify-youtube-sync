package org.example.domain.music

import org.example.domain.model.Song
import org.example.domain.model.SongMatchCandidate

object SongMatcher {

    private val commonNoisePatterns = listOf(
        "\\[.*?\\]",
        "\\(official.*?\\)",
        " - topic",
        "lyrics",
        "official video",
        "official audio",
        "mv",
        "\\(feat.*?\\)",
        "\\(with.*?\\)",
        " ft\\.[^()\\-]*", // Remove ft. and following until paren, hyphen or end
        " featuring [^()\\-]*"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    private val versionPatterns = listOf(
        "\\(remix\\)",
        " - remix",
        "\\(acoustic.*?\\)",
        " - acoustic.*?",
        "\\(live.*?\\)",
        " - live",
        "\\(taylor.s version\\)",
        " - taylor.s version",
        "\\(atl.s version\\)",
        " - atl.s version",
        "\\(from the room below\\)",
        " - from the room below"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    fun findBestMatch(original: Song, candidates: List<SongMatchCandidate>): SongMatchCandidate? {
        return candidates.firstOrNull { candidate -> matches(original, candidate) }
    }

    fun matches(original: Song, candidate: SongMatchCandidate): Boolean {
        val cleanedOriginalTitle = cleanCoreTitle(original.name.value)
        val originalVersionTags = extractVersionTags(original.name.value)
        val originalArtists = original.artists.map { it.value.lowercase() }.toSet()

        val cleanedCandidateTitle = cleanCoreTitle(candidate.title)
        val candidateVersionTags = extractVersionTags(candidate.title)

        // Primary Title Match
        val titleMatches = cleanedOriginalTitle == cleanedCandidateTitle ||
                (cleanedCandidateTitle.contains(cleanedOriginalTitle) && cleanedOriginalTitle.isNotBlank()) ||
                (cleanedOriginalTitle.contains(cleanedCandidateTitle) && cleanedCandidateTitle.isNotBlank())


        if (!titleMatches) return false

        // Artist Match
        val candidateText = (candidate.channelTitle + " " + candidate.title).lowercase()
        val artistMatches = originalArtists.any { artist -> candidateText.contains(artist) }

        if (!artistMatches) return false

        // Version Tag Match - this is crucial for distinguishing
        return originalVersionTags == candidateVersionTags
    }

    fun cleanCoreTitle(title: String): String {
        // Start by cleaning for canonical key (removes noise, keeps versions)
        var text = cleanTitleForCanonicalKey(title)
        
        // Then remove versions
        versionPatterns.forEach { regex ->
            text = text.replace(regex, "")
        }

        // Final polish for core title (remove special chars including parens and hyphens now)
        text = text.replace(Regex("[^a-z0-9 ]"), " ")

        return text.trim().replace(Regex("\\s+"), " ")
    }

    internal fun extractVersionTags(title: String): Set<String> {
        val tags = mutableSetOf<String>()
        val lowerTitle = title.lowercase()

        versionPatterns.forEach { regex ->
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

    fun cleanTitleForCanonicalKey(title: String): String {
        var text = title.lowercase()

        commonNoisePatterns.forEach { regex ->
            text = text.replace(regex, "")
        }

        // Normalize special characters
        text = text.replace("'", "") // Remove apostrophes
        // Replace intra-word hyphens (e.g. SONG-NAME) with space
        text = text.replace(Regex("(?<=[a-z0-9])-(?=[a-z0-9])"), " ")

        // Replace other special chars but keep parens and hyphens (for versions)
        text = text.replace(Regex("[^a-z0-9()\\- ]"), " ")

        return text.trim().replace(Regex("\\s+"), " ")
    }
}