package dev.gitfudge.audora.domain

/**
 * Parses an audio file's name into tag fields, offline and instantly. This is a
 * pure populator: it never writes — it produces a [FilenameParse] that the
 * detail screen turns into form edits (dirtying the existing propose → review
 * diff → write-to-confirm pipeline).
 *
 * Real-world filenames are messy: separators are inconsistent (` - `, `_`, `·`,
 * raw spaces), track numbers come with leading zeros, and the extension has to
 * be stripped. The supported [NamingPattern]s mirror the picker in the track
 * detail mock; [detectPattern] guesses the most likely one for a given name.
 */
enum class NamingPattern(val label: String, val tokenCount: Int) {
    TRACK_ARTIST_TITLE("Track · Artist · Title", 3),
    ARTIST_TITLE("Artist · Title", 2),
    TRACK_TITLE("Track · Title", 2),
    TITLE_ONLY("Title only", 1),
}

/** One token from the filename mapped onto a field (or ignored). */
data class TokenMapping(
    val token: String,
    /** Human label for the target field, e.g. "Title". Null when ignored. */
    val fieldLabel: String?,
    /** Resolved value as it will be set on the form (e.g. "4" for a track #). */
    val value: String?,
)

/**
 * The result of parsing [rawName] with a [pattern]: the field→value [fields] map
 * the form will apply, plus the per-token [mappings] the UI renders. [fields]
 * keys are the canonical field names: "title", "artist", "trackNumber".
 */
data class FilenameParse(
    val pattern: NamingPattern,
    val fields: Map<String, String>,
    val mappings: List<TokenMapping>,
)

object FilenameTags {

    /** Common token separators, longest/most-specific first. */
    private val SEPARATORS = listOf(" - ", " – ", " — ", " · ", "_", " -", "- ")

    /** Strips a trailing audio extension (case-insensitive) from [name]. */
    fun stripExtension(name: String): String {
        val dot = name.lastIndexOf('.')
        if (dot <= 0) return name
        val ext = name.substring(dot + 1).lowercase()
        return if (ext.length in 1..5 && ext.all { it.isLetterOrDigit() }) {
            name.substring(0, dot)
        } else {
            name
        }
    }

    /** The extension including the dot, e.g. ".flac", or "" when there is none. */
    private fun extensionOf(name: String): String {
        val stem = stripExtension(name)
        return if (stem.length < name.length) name.substring(stem.length) else ""
    }

    /**
     * Splits the stem into trimmed, non-empty tokens. Tries each separator in
     * priority order; falls back to splitting on runs of whitespace so a name
     * like "04 Luna Rail Night Bus" still yields usable parts.
     */
    fun tokenize(stem: String): List<String> {
        val trimmed = stem.trim()
        if (trimmed.isEmpty()) return emptyList()
        for (sep in SEPARATORS) {
            if (trimmed.contains(sep)) {
                val parts = trimmed.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 2) return parts
            }
        }
        return trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    /** Pulls a leading track number from [token], normalized without zero pad. */
    private fun parseTrackNumber(token: String): String? {
        val digits = token.trimStart().takeWhile { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()?.toString()
    }

    /** True when [token] looks like a bare leading track number (e.g. "04"). */
    private fun looksLikeTrackNumber(token: String): Boolean {
        val t = token.trim()
        return t.isNotEmpty() && t.length <= 3 && t.all { it.isDigit() }
    }

    /**
     * Guesses the best [NamingPattern] for [rawName]:
     * - 3 tokens with a leading number → Track · Artist · Title
     * - 2 tokens with a leading number → Track · Title
     * - 2 tokens without → Artist · Title
     * - otherwise → Title only
     * Whitespace-only splits never imply an Artist, so a name with no real
     * separators falls back to Title only.
     */
    fun detectPattern(rawName: String): NamingPattern {
        val stem = stripExtension(rawName)
        val hadSeparator = SEPARATORS.any { stem.contains(it) }
        val tokens = tokenize(stem)
        val leadingNumber = tokens.firstOrNull()?.let { looksLikeTrackNumber(it) } == true
        // Without a real separator we never invent an Artist or Track # — a bare
        // whitespace split (e.g. "04 Luna Rail Night Bus") stays Title only.
        return when {
            !hadSeparator -> NamingPattern.TITLE_ONLY
            tokens.size >= 3 && leadingNumber -> NamingPattern.TRACK_ARTIST_TITLE
            tokens.size == 2 && leadingNumber -> NamingPattern.TRACK_TITLE
            tokens.size >= 2 -> NamingPattern.ARTIST_TITLE
            else -> NamingPattern.TITLE_ONLY
        }
    }

    /**
     * Parses [rawName] with [pattern] (defaulting to [detectPattern]). Tokens
     * beyond what the pattern consumes are folded back into the final field
     * (usually the title) so nothing is silently dropped.
     */
    fun parse(rawName: String, pattern: NamingPattern = detectPattern(rawName)): FilenameParse {
        val stem = stripExtension(rawName)
        val ext = extensionOf(rawName)
        val tokens = tokenize(stem)
        val mappings = mutableListOf<TokenMapping>()
        val fields = linkedMapOf<String, String>()

        fun assign(label: String, key: String, raw: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value
                mappings.add(TokenMapping(raw, label, value))
            } else {
                mappings.add(TokenMapping(raw, null, null))
            }
        }

        when (pattern) {
            NamingPattern.TRACK_ARTIST_TITLE -> {
                if (tokens.isNotEmpty()) assign("Track #", "trackNumber", tokens[0], parseTrackNumber(tokens[0]))
                if (tokens.size >= 2) assign("Artist", "artist", tokens[1], tokens[1].trim())
                if (tokens.size >= 3) {
                    val title = tokens.drop(2).joinToString(" ").trim()
                    assign("Title", "title", tokens.drop(2).joinToString(" "), title)
                }
            }
            NamingPattern.ARTIST_TITLE -> {
                if (tokens.isNotEmpty()) assign("Artist", "artist", tokens[0], tokens[0].trim())
                if (tokens.size >= 2) {
                    val title = tokens.drop(1).joinToString(" ").trim()
                    assign("Title", "title", tokens.drop(1).joinToString(" "), title)
                }
            }
            NamingPattern.TRACK_TITLE -> {
                if (tokens.isNotEmpty()) assign("Track #", "trackNumber", tokens[0], parseTrackNumber(tokens[0]))
                if (tokens.size >= 2) {
                    val title = tokens.drop(1).joinToString(" ").trim()
                    assign("Title", "title", tokens.drop(1).joinToString(" "), title)
                }
            }
            NamingPattern.TITLE_ONLY -> {
                val title = stem.trim()
                assign("Title", "title", stem, title)
            }
        }

        if (ext.isNotEmpty()) {
            mappings.add(TokenMapping(ext, null, null))
        }

        return FilenameParse(pattern, fields, mappings)
    }
}
