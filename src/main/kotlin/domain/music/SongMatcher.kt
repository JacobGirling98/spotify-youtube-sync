package org.example.domain.music

import org.example.domain.model.Song
import org.example.domain.model.SongMatchCandidate

data class MatchResult(
    val titleMatches: Boolean,
    val allArtistsMatch: Boolean,
    val atLeastOneArtistMatches: Boolean,
    val versionTagMatches: Boolean
)

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
        "\\(rock version\\)",
        " - rock version",
        "\\(taylor(?:'|\\s)*s version\\)",
        " - taylor(?:'|\\s)*s version",
        "\\(atl(?:'|\\s)*s version\\)",
        " - atl(?:'|\\s)*s version",
        "\\(from the room below\\)",
        " - from the room below"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    fun findBestMatch(original: Song, candidates: List<SongMatchCandidate>): SongMatchCandidate? {
        return candidates.firstOrNull { candidate -> matches(original, candidate) }
    }

    fun matches2(original: Song, candidate: SongMatchCandidate): MatchResult {
        val titleMatches = original.name.value.equals(candidate.title, ignoreCase = true)
        val artistsMatch =
            original.artists.joinToString(", ") { it.value }.equals(candidate.channelTitle, ignoreCase = true)
        val atLeastOneArtistMatches = true
        val versionTagMatches = true
        return MatchResult(
            titleMatches = titleMatches,
            allArtistsMatch = artistsMatch,
            atLeastOneArtistMatches = atLeastOneArtistMatches,
            versionTagMatches = versionTagMatches
        )
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
        var text = cleanTitleForCanonicalKey(title)

        versionPatterns.forEach { regex ->
            text = text.replace(regex, "")
        }

        // Final polish for core title (remove special chars including parens and hyphens now)
        text = text.replace(Regex("""[()\-]"""), " ")

        return text.trim().replace(Regex("\\s+"), " ")
    }

    internal fun extractVersionTags(title: String): Set<String> {
        val tags = mutableSetOf<String>()
        val lowerTitle = title.decodeHtmlEntities().lowercase()

        versionPatterns.forEach { regex ->
            regex.findAll(lowerTitle).forEach { match ->
                val tag = match.value.lowercase()
                    .replace(Regex("[^a-z0-9& ]"), "") // Remove punctuation
                    .trim()
                if (tag.isNotBlank()) {
                    tags.add(tag.replace(Regex("\\s+"), " ")) // Normalize spaces and add
                }
            }
        }
        return tags
    }

    fun cleanTitleForCanonicalKey(title: String): String = title
        .decodeHtmlEntities()
        .lowercase()
        .let { title -> commonNoisePatterns.fold(title) { title, regex -> title.replace(regex, "") } }
        .replace("'", "")
        .replace(Regex("(?<=[a-z0-9])-(?=[a-z0-9])"), " ")
        .replace(Regex("[^a-z0-9()\\-& ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.decodeHtmlEntities(): String = this
        .replace("&amp;", "&")
        .replace("&#38;", "&")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&quot;", "\"")
        .replace("&#34;", "\"")
        .replace("&lt;", "<")
        .replace("&#60;", "<")
        .replace("&gt;", ">")
        .replace("&#62;", ">")
}