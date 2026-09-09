package dev.avelissesolutions.avelisse.journal

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import dev.avelissesolutions.avelisse.core.theme.AvelisseTouch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** What the save dialog creates. Plain text so anything can open it. */
private const val EXPORT_MIME_TYPE = "text/plain"

/** Smallest height of a tappable row. Nothing on this screen is smaller. */
private val ROW_MIN_HEIGHT = 72.dp

/**
 * Everything that has been said into either log, newest first.
 *
 * One screen rather than a list plus a detail page: tapping a row opens the full text in
 * place. Someone reading back a bad week should not have to navigate in and out of a
 * dozen pages to do it.
 *
 * @param onBack Returns to Home.
 */
@Composable
fun EntriesScreen(
    onBack: () -> Unit,
    viewModel: EntriesViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val formatter = remember { entryDateFormatter() }
    val title = stringResource(R.string.entries_title)
    val symptomLabel = stringResource(R.string.entries_kind_symptom)
    val visitLabel = stringResource(R.string.entries_kind_visit)

    // Held between opening the save dialog and the person choosing a destination, so what
    // gets written is what was on screen when they pressed Export.
    var pendingExport by remember { mutableStateOf("") }

    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(EXPORT_MIME_TYPE),
    ) { destination ->
        // Null means they backed out of the save dialog. Nothing to write, nothing to say.
        if (destination != null) {
            JournalExporter.writeTo(context, destination, pendingExport)
        }
    }

    EntriesScreenContent(
        entries = entries,
        query = query,
        onQueryChange = viewModel::setQuery,
        onDelete = viewModel::deleteEntry,
        // Exports what is on screen: everything, or only what the search narrowed it to.
        onExport = {
            pendingExport = JournalExporter.buildExportText(title, entries) { entry ->
                val kind = when (entry.kind) {
                    JournalKind.SYMPTOM -> symptomLabel
                    JournalKind.VISIT -> visitLabel
                }
                formatter.format(Instant.ofEpochMilli(entry.createdAt)) + " - " + kind
            }
            saveFile.launch(JournalExporter.suggestedFileName(LocalDate.now()))
        },
        onBack = onBack,
    )
}

/**
 * The screen without its ViewModel, so the layout can be exercised on its own.
 */
@Composable
internal fun EntriesScreenContent(
    entries: List<JournalEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: () -> Unit,
    onBack: () -> Unit,
) {
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeletion by remember { mutableStateOf<JournalEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvelisseColors.Background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.entries_back_cd),
                    tint = AvelisseColors.TextPrimary,
                )
            }
            Text(
                text = stringResource(R.string.entries_title),
                color = AvelisseColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (entries.isNotEmpty()) {
                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.entries_export_cd),
                        tint = AvelisseColors.TextPrimary,
                    )
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.entries_search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = AvelisseColors.TextSecondary,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AvelisseColors.Surface,
                unfocusedContainerColor = AvelisseColors.Surface,
                focusedBorderColor = AvelisseColors.Primary,
                unfocusedBorderColor = AvelisseColors.Border,
                focusedTextColor = AvelisseColors.TextPrimary,
                unfocusedTextColor = AvelisseColors.TextPrimary,
                focusedPlaceholderColor = AvelisseColors.TextSecondary,
                unfocusedPlaceholderColor = AvelisseColors.TextSecondary,
                cursorColor = AvelisseColors.Primary,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (entries.isEmpty()) {
            EmptyState(
                message = if (query.isBlank()) {
                    stringResource(R.string.entries_empty)
                } else {
                    stringResource(R.string.entries_no_results)
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AvelisseColors.Surface)
                    .border(1.dp, AvelisseColors.Border, RoundedCornerShape(16.dp)),
            ) {
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    if (index > 0) {
                        HorizontalDivider(thickness = 1.dp, color = AvelisseColors.Border)
                    }
                    EntryRow(
                        entry = entry,
                        isExpanded = expandedId == entry.id,
                        onToggle = {
                            expandedId = if (expandedId == entry.id) null else entry.id
                        },
                        onDelete = { pendingDeletion = entry },
                    )
                }
            }
        }
    }

    pendingDeletion?.let { entry ->
        DeleteConfirmation(
            onConfirm = {
                onDelete(entry.id)
                pendingDeletion = null
            },
            onDismiss = { pendingDeletion = null },
        )
    }
}

/**
 * One entry. Collapsed it shows the opening line and when it was said; expanded it shows
 * everything, plus the only way to delete it.
 */
@Composable
private fun EntryRow(
    entry: JournalEntry,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ROW_MIN_HEIGHT)
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(entry.kind.dotColor()),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deriveTitle(entry.text),
                    color = AvelisseColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Which log this belongs to is written out, not left to the colour of
                // the dot. Anyone who cannot separate the teal from the terracotta -
                // and that is a fair share of people - could not otherwise tell a
                // symptom entry from an appointment one at all.
                Text(
                    text = stringResource(entry.kind.labelRes()) + " · " +
                        formatEntryTimestamp(entry.createdAt),
                    color = AvelisseColors.TextSecondary,
                    fontSize = 13.sp,
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = AvelisseColors.TextSecondary,
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(start = 48.dp, end = 20.dp, bottom = 12.dp)) {
                Text(
                    text = entry.text,
                    color = AvelisseColors.TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.heightIn(min = AvelisseTouch.Minimum),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = AvelisseColors.Destructive,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.entries_delete),
                        color = AvelisseColors.Destructive,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * Deleting is the one thing here that cannot be undone, and the person doing it may have
 * a shaky hand, so it asks first.
 */
@Composable
private fun DeleteConfirmation(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AvelisseColors.Surface,
        titleContentColor = AvelisseColors.TextPrimary,
        textContentColor = AvelisseColors.TextSecondary,
        title = { Text(stringResource(R.string.entries_delete_title)) },
        text = { Text(stringResource(R.string.entries_delete_body)) },
        // Both actions live in this one slot, stacked, rather than in the usual
        // confirm/dismiss pair. Side by side they were two small pieces of text a
        // thumb's width apart, with the irreversible one bolder and on the side the
        // thumb rests. Keeping is the large, obvious target; deleting is deliberately
        // the quieter one, and far enough away that a bad tap misses it entirely.
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AvelisseTouch.Primary)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AvelisseColors.Primary)
                        .clickable(role = Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.entries_keep),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(AvelisseTouch.Separation * 2))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AvelisseTouch.Minimum)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClick = onConfirm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.entries_delete_confirm),
                        color = AvelisseColors.Destructive,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        },
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = message,
            color = AvelisseColors.TextSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 48.dp),
        )
    }
}

/** Teal for the symptom log, terracotta for the appointment log. */
@Composable
private fun JournalKind.dotColor() = when (this) {
    JournalKind.SYMPTOM -> AvelisseColors.Primary
    JournalKind.VISIT -> AvelisseColors.Secondary
}

/** When the entry was said, in the reader's own language and date order. */
private fun entryDateFormatter(): DateTimeFormatter =
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

@Composable
private fun formatEntryTimestamp(epochMillis: Long): String {
    val formatter = remember { entryDateFormatter() }
    return formatter.format(Instant.ofEpochMilli(epochMillis))
}

/** The name of the log an entry belongs to, for the list to say out loud. */
private fun JournalKind.labelRes(): Int = when (this) {
    JournalKind.SYMPTOM -> R.string.entries_kind_symptom
    JournalKind.VISIT -> R.string.entries_kind_visit
}
