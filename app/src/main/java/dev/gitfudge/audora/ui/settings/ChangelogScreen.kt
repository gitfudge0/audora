package dev.gitfudge.audora.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.data.releases.AppRelease
import dev.gitfudge.audora.ui.ReleasesUiState
import dev.gitfudge.audora.ui.UpdateStatus
import dev.gitfudge.audora.ui.components.AppPanel
import dev.gitfudge.audora.ui.components.AppTopBar
import dev.gitfudge.audora.ui.theme.AppTextStyles
import dev.gitfudge.audora.ui.theme.LocalSpacing

@Composable
fun ChangelogScreen(
    releasesState: ReleasesUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopBar(
                title = "Changelog",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh changelog")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            releasesState.isLoading && releasesState.releases.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.size(spacing.md))
                    Text("Loading GitHub releases", color = colors.onSurfaceVariant)
                }
            }

            releasesState.releases.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = releasesState.errorMessage ?: "No releases found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(spacing.md))
                    Button(onClick = onRefresh) {
                        Text("Try again")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = spacing.lg,
                        vertical = spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    releasesState.errorMessage?.let { message ->
                        item(key = "error") {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.error,
                            )
                        }
                    }
                    items(
                        items = releasesState.releases,
                        key = { it.version },
                        contentType = { "release" },
                    ) { release ->
                        ReleaseCard(
                            release = release,
                            isUpdate = release.version == releasesState.updateRelease?.version,
                            updateStatus = releasesState.updateStatus,
                            onInstallUpdate = onInstallUpdate,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsNewDialog(
    release: AppRelease,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What's new in ${release.version}") },
        text = {
            val blocks = remember(release.notes) { parseReleaseMarkdown(release.notes) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "title") {
                    Text(
                        text = release.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                items(
                    items = blocks,
                    key = { it.key },
                    contentType = { it.contentType },
                ) { block ->
                    MarkdownBlock(block = block)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun ReleaseCard(
    release: AppRelease,
    isUpdate: Boolean,
    updateStatus: UpdateStatus,
    onInstallUpdate: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    AppPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = release.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = release.version,
                        style = AppTextStyles.mono,
                        color = colors.onSurfaceVariant,
                    )
                }
                if (release.prerelease) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.tertiary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            release.publishedAt?.let {
                Text(
                    text = it.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            if (isUpdate) {
                Button(
                    onClick = onInstallUpdate,
                    enabled = updateStatus != UpdateStatus.Downloading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (updateStatus == UpdateStatus.Downloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.size(spacing.sm))
                    Text(
                        if (updateStatus == UpdateStatus.Downloading) {
                            "Downloading"
                        } else {
                            "Download and install"
                        },
                    )
                }
            }
            ReleaseMarkdown(notes = release.notes)
        }
    }
}

@Composable
private fun ReleaseMarkdown(notes: String) {
    val blocks = remember(notes) { parseReleaseMarkdown(notes) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            MarkdownBlock(block = block)
        }
    }
}

@Composable
private fun MarkdownBlock(block: MarkdownBlockModel) {
    val colors = MaterialTheme.colorScheme
    when (block) {
        is MarkdownBlockModel.Heading -> Text(
            text = inlineMarkdown(block.text),
            style = when (block.level) {
                1 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            },
            color = colors.onSurface,
        )

        is MarkdownBlockModel.Bullet -> Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodySmall,
                color = colors.tertiary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = inlineMarkdown(block.text),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        is MarkdownBlockModel.Code -> Text(
            text = block.text,
            style = AppTextStyles.monoSmall,
            color = colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceVariant, MaterialTheme.shapes.extraSmall)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )

        is MarkdownBlockModel.Paragraph -> Text(
            text = inlineMarkdown(block.text),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Immutable
private sealed interface MarkdownBlockModel {
    val key: String
    val contentType: String

    @Immutable
    data class Heading(
        override val key: String,
        val level: Int,
        val text: String,
    ) : MarkdownBlockModel {
        override val contentType: String = "heading"
    }

    @Immutable
    data class Bullet(
        override val key: String,
        val text: String,
    ) : MarkdownBlockModel {
        override val contentType: String = "bullet"
    }

    @Immutable
    data class Paragraph(
        override val key: String,
        val text: String,
    ) : MarkdownBlockModel {
        override val contentType: String = "paragraph"
    }

    @Immutable
    data class Code(
        override val key: String,
        val text: String,
    ) : MarkdownBlockModel {
        override val contentType: String = "code"
    }
}

private fun parseReleaseMarkdown(markdown: String): List<MarkdownBlockModel> {
    val blocks = mutableListOf<MarkdownBlockModel>()
    val paragraph = StringBuilder()
    var codeBlock: StringBuilder? = null

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) {
            blocks += MarkdownBlockModel.Paragraph(
                key = "p-${blocks.size}",
                text = text,
            )
        }
        paragraph.clear()
    }

    markdown.lineSequence().forEach { rawLine ->
        val line = rawLine.trimEnd()
        val activeCodeBlock = codeBlock
        when {
            line.trimStart().startsWith("```") && activeCodeBlock == null -> {
                flushParagraph()
                codeBlock = StringBuilder()
            }

            line.trimStart().startsWith("```") && activeCodeBlock != null -> {
                blocks += MarkdownBlockModel.Code(
                    key = "code-${blocks.size}",
                    text = activeCodeBlock.toString().trimEnd(),
                )
                codeBlock = null
            }

            activeCodeBlock != null -> {
                activeCodeBlock.appendLine(rawLine)
            }

            line.isBlank() -> flushParagraph()

            line.startsWith("#") -> {
                val headingText = line.dropWhile { it == '#' }.trim()
                if (headingText.isNotEmpty()) {
                    flushParagraph()
                    blocks += MarkdownBlockModel.Heading(
                        key = "h-${blocks.size}",
                        level = line.takeWhile { it == '#' }.length,
                        text = headingText,
                    )
                }
            }

            line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                flushParagraph()
                blocks += MarkdownBlockModel.Bullet(
                    key = "li-${blocks.size}",
                    text = line.trimStart().drop(2).trim(),
                )
            }

            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line.trim())
            }
        }
    }

    codeBlock?.let {
        blocks += MarkdownBlockModel.Code(
            key = "code-${blocks.size}",
            text = it.toString().trimEnd(),
        )
    }
    flushParagraph()

    return blocks.ifEmpty {
        listOf(MarkdownBlockModel.Paragraph(key = "p-empty", text = "No release notes published."))
    }
}

@Composable
private fun inlineMarkdown(text: String): AnnotatedString {
    val colors = MaterialTheme.colorScheme
    return remember(text, colors.primary, colors.tertiaryContainer) {
        buildAnnotatedString {
            appendInlineMarkdown(
                source = text,
                linkStyle = SpanStyle(
                    color = colors.primary,
                    fontWeight = FontWeight.Medium,
                ),
                codeStyle = SpanStyle(
                    color = colors.onSurface,
                    background = colors.surfaceVariant,
                    fontFamily = FontFamily.Monospace,
                ),
                strongStyle = SpanStyle(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(
    source: String,
    linkStyle: SpanStyle,
    codeStyle: SpanStyle,
    strongStyle: SpanStyle,
) {
    var index = 0
    while (index < source.length) {
        when {
            source.startsWith("**", index) -> {
                val end = source.indexOf("**", startIndex = index + 2)
                if (end > index) {
                    withStyle(strongStyle) { append(source.substring(index + 2, end)) }
                    index = end + 2
                } else {
                    append(source[index])
                    index++
                }
            }

            source[index] == '`' -> {
                val end = source.indexOf('`', startIndex = index + 1)
                if (end > index) {
                    withStyle(codeStyle) { append(source.substring(index + 1, end)) }
                    index = end + 1
                } else {
                    append(source[index])
                    index++
                }
            }

            source[index] == '[' -> {
                val labelEnd = source.indexOf(']', startIndex = index + 1)
                val urlStart = labelEnd + 1
                if (
                    labelEnd > index &&
                    urlStart < source.length &&
                    source[urlStart] == '('
                ) {
                    val urlEnd = source.indexOf(')', startIndex = urlStart + 1)
                    if (urlEnd > urlStart) {
                        withStyle(linkStyle) { append(source.substring(index + 1, labelEnd)) }
                        index = urlEnd + 1
                    } else {
                        append(source[index])
                        index++
                    }
                } else {
                    append(source[index])
                    index++
                }
            }

            else -> {
                append(source[index])
                index++
            }
        }
    }
}
