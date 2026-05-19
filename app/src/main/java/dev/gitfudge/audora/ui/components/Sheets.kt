package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.gitfudge.audora.ui.theme.LocalShapeScale

/**
 * Bottom sheet shell. surfaceElevated background, hairline top, xl radius
 * top corners only; title row, content slot, optional footer action row.
 *
 * `windowInsets = WindowInsets(0)` keeps callers in control of insets so
 * the sheet sits flush above the system bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val xlTop = LocalShapeScale.current.xl.let {
        // Apply only to top corners.
        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = xlTop,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.imePadding()) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (titleTrailing != null) titleTrailing()
                }
                Hairline()
            }
            // Content takes the remaining space and scrolls internally so the
            // footer stays pinned and visible even with the keyboard open.
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(contentPadding),
            ) { content() }
            if (footer != null) {
                Hairline()
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { footer() }
            }
            // Bottom inset breathing room
            Box(modifier = Modifier.height(8.dp))
        }
    }
}
