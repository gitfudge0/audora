package dev.gitfudge.musicworkbench.domain

/**
 * Album grouping key. Matches MediaScanner.kt: album-artist (falling back to
 * artist) concatenated with album, lowercased, no separator.
 */
fun buildAlbumKey(albumArtist: String?, artist: String?, album: String?): String {
    val groupArtist = (albumArtist ?: artist ?: "").lowercase()
    val groupAlbum = album.orEmpty().lowercase()
    return "$groupArtist$groupAlbum"
}
