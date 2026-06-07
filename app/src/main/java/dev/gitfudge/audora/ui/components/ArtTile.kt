package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.gitfudge.audora.ui.theme.LocalShapeScale
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

/**
 * Album/track art tile. Sizes:
 *   sm = 40dp (track row leading)
 *   md = 56dp (album row leading)
 *   lg = 96dp (small hero, picker preview)
 *   Hero = caller controls size (fills width).
 *
 * Shape grows with size per spec: sm/md use sm radius, lg uses md, hero uses
 * lg/xl depending on placement.
 */
enum class ArtTileSize(val dp: Dp) {
    Sm(40.dp),
    Md(64.dp),
    Lg(96.dp),
}

@Composable
fun ArtTile(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: ArtTileSize = ArtTileSize.Md,
    shape: RoundedCornerShape = when (size) {
        ArtTileSize.Sm, ArtTileSize.Md -> LocalShapeScale.current.sm
        ArtTileSize.Lg -> LocalShapeScale.current.md
    },
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (model == null) {
            ArtPlaceholder()
        } else {
            val context = LocalContext.current
            // Rebuilding the request every recomposition allocates on each scroll
            // frame for every visible tile; key it on the model so it's stable.
            val request = remember(context, model) {
                ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(false)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** Hero (width-bound) variant — caller sets size via modifier. */
@Composable
fun ArtTileHero(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = LocalShapeScale.current.xl,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (model == null) {
            ArtPlaceholder(iconSize = 64.dp)
        } else {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun ArtPlaceholder(iconSize: Dp = 20.dp) {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(iconSize),
    )
}

@Composable
private fun ArtTilesPreviewContent() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtTile(model = null, contentDescription = null, size = ArtTileSize.Sm)
            ArtTile(model = null, contentDescription = null, size = ArtTileSize.Md)
            ArtTile(model = null, contentDescription = null, size = ArtTileSize.Lg)
        }
    }
}

@Preview(name = "ArtTile · Light") @Composable
private fun ArtTileLightPreview() = AudoraTheme(themeMode = ThemeMode.Light) { ArtTilesPreviewContent() }

@Preview(name = "ArtTile · Dark") @Composable
private fun ArtTileDarkPreview() = AudoraTheme(themeMode = ThemeMode.Dark) { ArtTilesPreviewContent() }
