package dev.gitfudge.audora.domain

import dev.gitfudge.audora.data.db.TrackEntity

enum class ArtStatus { PENDING, NONE, LOW_RES, OK }

enum class LyricsStatus { NONE, SIDECAR_PLAIN, SIDECAR_SYNCED }

enum class TagStatus { OK, INCOMPLETE, UNKNOWN_ARTIST }

enum class LibraryFilter { ALL, MISSING_ART, LOW_RES_ART, NO_LYRICS, INCOMPLETE_TAGS, UNKNOWN_ARTIST, WRONG_EXTENSION, DUPLICATES }

enum class LibrarySort { ALBUM, TITLE, ARTIST, RECENTLY_MODIFIED }

fun TrackEntity.artStatus(lowResThresholdPx: Int): ArtStatus = when {
    artScanPending -> ArtStatus.PENDING
    !hasEmbeddedArt -> ArtStatus.NONE
    (artWidth ?: 0) == 0 || (artHeight ?: 0) == 0 -> ArtStatus.OK
    maxOf(artWidth ?: 0, artHeight ?: 0) < lowResThresholdPx -> ArtStatus.LOW_RES
    else -> ArtStatus.OK
}

fun TrackEntity.lyricsStatus(): LyricsStatus = when {
    !hasSidecarLrc -> LyricsStatus.NONE
    sidecarLrcSynced -> LyricsStatus.SIDECAR_SYNCED
    else -> LyricsStatus.SIDECAR_PLAIN
}

fun TrackEntity.tagStatus(): TagStatus = when {
    artistUnknown -> TagStatus.UNKNOWN_ARTIST
    !coreTagsComplete -> TagStatus.INCOMPLETE
    else -> TagStatus.OK
}

/** Title shown in the list: real tag title, else the filename without extension. */
fun TrackEntity.displayTitle(): String =
    title?.takeIf { it.isNotBlank() } ?: displayName.substringBeforeLast('.')

fun TrackEntity.displayArtist(): String? = artist?.takeIf { it.isNotBlank() }
