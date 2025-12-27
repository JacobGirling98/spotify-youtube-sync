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

    fun cleanCoreTitle(title: String): String {
        var text = title.lowercase()
        
        // Remove all common noise, featured artists, and version tags for the most basic core title
        val noiseAndTagsPatterns = listOf(
            "\\(feat.*?\\)", // (feat. X)
            "\\(with.*?\\)", // (with X)
            "\\[.*?\\]", // [Official Video], [Lyrics]
            "\\(official.*?\\)", // (Official Audio)
            " - topic", // YouTube Topic channels suffix
            "lyrics",
            "official video",
            "official audio",
            "mv", // Music video tag
            "\\(remix\\)", // (Remix)
            " - remix", // - Remix
            "\\(acoustic.*?\\)", // (Acoustic Version)
            " - acoustic.*?", // - Acoustic Version
            "\\(live.*?\\)", // (Live)
            " - live", // - Live
            "\\(taylor.s version\\)",
            " - taylor.s version",
            "\\(atl.s version\\)",
            " - atl.s version",
            "\\(from the room below\\)",
            " - from the room below",
            " ft\\.", // ft.
            " featuring" // featuring
        )

        noiseAndTagsPatterns.forEach { pattern ->
            text = text.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove special characters, keep alphanumeric and spaces
        text = text.replace("'", "") // Remove apostrophes here for core matching
        text = text.replace(Regex("[^a-z0-9 ]"), " ") // Replace non-alphanumeric with space

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
            // Featured artists are not version tags, they are handled in cleanCoreTitle
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

    fun cleanTitleForCanonicalKey(title: String): String {
        var text = title.lowercase()

        // Remove noise (Official Video, Lyrics, etc.)
        val noisePatterns = listOf(
            "\\[.*?\\]",
            "\\(official.*?\\)",
            " - topic",
            "lyrics",
            "official video",
            "official audio",
            "mv"
        )
        noisePatterns.forEach { pattern ->
            text = text.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove featured artists and with
        val featPatterns = listOf(
            "\\(feat.*?\\)",
            "\\(with.*?\\)"
        )
        featPatterns.forEach { pattern ->
            text = text.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove 'ft.' and 'featuring' and everything after
        text = text.replace(Regex(" ft\\..*", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex(" featuring .*", RegexOption.IGNORE_CASE), "")

        // Normalize special characters
        text = text.replace("'", "") // Remove apostrophes
        // Replace intra-word hyphens (e.g. SONG-NAME) with space
        text = text.replace(Regex("(?<=[a-z0-9])-(?=[a-z0-9])"), " ")

        // Replace other special chars but keep parens and hyphens (for versions)
        text = text.replace(Regex("[^a-z0-9()\\- ]"), " ")

        return text.trim().replace(Regex("\\s+"), " ")
    }
}