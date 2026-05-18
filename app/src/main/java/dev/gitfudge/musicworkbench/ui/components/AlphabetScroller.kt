package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import kotlinx.coroutines.launch

/** '#' captures everything that does not start with A–Z (digits, symbols, CJK…). */
val AlphabetScrollerLetters: List<Char> = listOf('#') + ('A'..'Z')

/**
 * Maps a section letter (uppercased first char; non A–Z folds to '#') for any
 * label, so callers can build the `indexForLetter` lookup consistently.
 */
fun sectionLetterOf(label: String): Char {
    val c = label.trim().firstOrNull()?.uppercaseChar() ?: '#'
    return if (c in 'A'..'Z') c else '#'
}

/**
 * Vertical A–Z fast-scroll rail pinned to the right edge of a list.
 *
 * A circular selector tracks the letter currently at the top of the list as it
 * scrolls. Tapping or dragging that selector jumps [listState] to the first
 * item in the touched letter's section. Letters with no items resolve to the
 * nearest populated section so a drag never dead-ends.
 *
 * Place inside a Box that also holds the LazyColumn, aligned to CenterEnd.
 */
@Composable
fun AlphabetScroller(
    listState: LazyListState,
    indexForLetter: (Char) -> Int?,
    modifier: Modifier = Modifier,
) {
    val letters = AlphabetScrollerLetters
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var railHeightPx by remember { mutableIntStateOf(0) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragging by remember { mutableStateOf(false) }

    // (letterIndex -> first item index) for every populated section, ascending.
    val sections = remember(indexForLetter) {
        letters.mapIndexedNotNull { i, c -> indexForLetter(c)?.let { i to it } }
    }

    // Which letter the list is parked on right now: the last section whose
    // start index is at or above the first visible item.
    val scrollLetterIndex by remember {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            sections.lastOrNull { it.second <= first }?.first
                ?: sections.firstOrNull()?.first ?: 0
        }
    }
    val selectedIndex = if (dragging) draggedIndex else scrollLetterIndex

    // Resolve a letter to a target item, falling back to the nearest earlier
    // populated section so empty letters still scroll somewhere sensible.
    fun resolveIndex(letterIdx: Int): Int? {
        for (i in letterIdx downTo 0) indexForLetter(letters[i])?.let { return it }
        for (i in letterIdx + 1 until letters.size) indexForLetter(letters[i])?.let { return it }
        return null
    }

    fun selectAt(y: Float) {
        if (railHeightPx <= 0) return
        val idx = ((y / railHeightPx) * letters.size).toInt()
            .coerceIn(0, letters.size - 1)
        if (idx == draggedIndex) return
        draggedIndex = idx
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        resolveIndex(idx)?.let { target ->
            scope.launch { listState.scrollToItem(target) }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        Column(
            modifier = Modifier
                .padding(end = spacing.xs)
                .width(48.dp)
                .fillMaxHeight(0.92f)
                .onSizeChanged { railHeightPx = it.height }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            dragging = true
                            selectAt(it.y)
                            tryAwaitRelease()
                            dragging = false
                            draggedIndex = -1
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragging = true; selectAt(it.y) },
                        onDragEnd = { dragging = false; draggedIndex = -1 },
                        onDragCancel = { dragging = false; draggedIndex = -1 },
                        onVerticalDrag = { change, _ -> selectAt(change.position.y) },
                    )
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            letters.forEachIndexed { i, ch ->
                val isSelected = i == selectedIndex
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.primary else androidx.compose.ui.graphics.Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ch.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                    )
                }
            }
        }

        // Large tooltip of the active letter — shown only while dragging the
        // rail — offset left of the rail so neither the finger nor the rail
        // covers it.
        if (dragging && selectedIndex in letters.indices) {
            Surface(
                color = colors.primary,
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(end = spacing.xl + spacing.xl)
                    .size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = letters[selectedIndex].toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimary,
                    )
                }
            }
        }
    }
}
