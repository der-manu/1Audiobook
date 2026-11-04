package voice.folderPicker.libraryImport

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.squareup.anvil.annotations.ContributesTo
import voice.common.AppScope
import voice.common.compose.rememberScoped
import voice.common.navigation.Destination
import voice.common.rootComponentAs
import voice.data.ChapterGrouping
import voice.data.ChapterSortKey
import voice.data.ChapterSortSettings
import voice.data.library.BookFlag
import voice.data.library.BookKind
import voice.data.library.LibraryPreview
import voice.common.R as CommonR
import voice.strings.R as StringsR

/** So viele Zeilen zeigt die Reihenfolge-Vorschau eines Buchs maximal. */
private const val MAX_PREVIEW_ROWS = 300

@ContributesTo(AppScope::class)
interface LibraryImportComponent {
  val libraryImportViewModelFactory: LibraryImportViewModel.Factory
}

@Composable
fun LibraryImport(
  uri: Uri,
  mode: Destination.SelectFolderType.Mode,
) {
  val viewModel = rememberScoped {
    rootComponentAs<LibraryImportComponent>().libraryImportViewModelFactory.create(uri, mode)
  }
  LibraryImportScreen(
    state = viewModel.viewState(),
    viewModel = viewModel,
  )
}

@Composable
private fun LibraryImportScreen(
  state: LibraryImportState,
  viewModel: LibraryImportViewModel,
) {
  val top = state.paddings.substringBefore(';').toInt()
  val bottom = state.paddings.substringAfter(';').substringBefore(';').toInt()
  val start = state.paddings.substringBeforeLast(';').substringAfterLast(';').toInt()
  val end = state.paddings.substringAfterLast(';').toInt()

  var renamingId by remember { mutableStateOf<String?>(null) }
  var detailId by remember { mutableStateOf<String?>(null) }

  Scaffold(
    modifier = Modifier.padding(start = start.dp, end = end.dp),
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = CommonR.string.library_import_title)) },
        navigationIcon = {
          IconButton(onClick = viewModel::cancel) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
              contentDescription = stringResource(id = StringsR.string.close),
            )
          }
        },
        windowInsets = WindowInsets(top = top.dp),
      )
    },
    bottomBar = {
      Surface(tonalElevation = 3.dp) {
        Button(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + bottom.dp),
          enabled = !state.scanning && !state.saving && state.selectedCount > 0,
          onClick = viewModel::confirm,
        ) {
          Text(
            text = if (state.saving) {
              stringResource(id = CommonR.string.library_import_saving)
            } else {
              stringResource(CommonR.string.library_import_add, state.selectedCount)
            },
          )
        }
      }
    },
  ) { contentPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding),
    ) {
      when {
        state.scanning -> ScanningContent(scannedFiles = state.scannedFiles)
        state.books.isEmpty() -> Text(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp),
          text = stringResource(id = CommonR.string.library_import_empty),
          color = MaterialTheme.colorScheme.error,
        )
        else -> BookList(
          state = state,
          viewModel = viewModel,
          onRename = { renamingId = it },
          onDetails = { detailId = it },
        )
      }
    }
  }

  val renaming = renamingId?.let { id -> state.books.firstOrNull { it.id == id } }
  if (renaming != null) {
    RenameDialog(
      initial = renaming.name,
      onConfirm = { name ->
        viewModel.rename(renaming.id, name)
        renamingId = null
      },
      onDismiss = { renamingId = null },
    )
  }
  val detail = detailId?.let { id -> state.books.firstOrNull { it.id == id } }
  if (detail != null) {
    OrderDialog(
      book = detail,
      onApply = { usePlaylist, sort ->
        viewModel.setOrder(detail.id, usePlaylist, sort)
        detailId = null
      },
      onDismiss = { detailId = null },
    )
  }
}

@Composable
private fun ScanningContent(scannedFiles: Int) {
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      CircularProgressIndicator(modifier = Modifier.size(48.dp))
      Text(text = stringResource(CommonR.string.library_import_scanning, scannedFiles))
    }
  }
}

@Composable
private fun BookList(
  state: LibraryImportState,
  viewModel: LibraryImportViewModel,
  onRename: (String) -> Unit,
  onDetails: (String) -> Unit,
) {
  val flagTexts = FlagTexts(
    few = stringResource(CommonR.string.library_flag_few),
    many = stringResource(CommonR.string.library_flag_many),
    mixed = stringResource(CommonR.string.library_flag_mixed),
    part = stringResource(CommonR.string.library_flag_part),
    playlistDefective = stringResource(CommonR.string.library_flag_playlist_defective),
  )
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item(key = "header") {
      Column {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
          Text(
            text = stringResource(CommonR.string.library_import_summary, state.books.size, state.reviewCount),
            style = MaterialTheme.typography.titleMedium,
          )
          if (state.looseRootFiles > 0) {
            Text(
              modifier = Modifier.padding(top = 4.dp),
              text = stringResource(CommonR.string.library_import_loose, state.looseRootFiles),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.setAllSelected(true) }) {
              Text(text = stringResource(CommonR.string.library_import_select_all))
            }
            TextButton(onClick = { viewModel.setAllSelected(false) }) {
              Text(text = stringResource(CommonR.string.library_import_select_none))
            }
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.setOnlyReview(!state.onlyReview) },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              modifier = Modifier.weight(1f),
              text = stringResource(CommonR.string.library_import_only_review),
            )
            Switch(
              checked = state.onlyReview,
              onCheckedChange = { viewModel.setOnlyReview(it) },
            )
          }
        }
        HorizontalDivider()
      }
    }
    items(state.visibleBooks, key = { it.id }) { book ->
      BookRow(
        book = book,
        flagTexts = flagTexts,
        onToggle = { viewModel.toggle(book.id) },
        onRename = { onRename(book.id) },
        onDetails = { onDetails(book.id) },
      )
    }
  }
}

private data class FlagTexts(
  val few: String,
  val many: String,
  val mixed: String,
  val part: String,
  val playlistDefective: String,
)

@Composable
private fun BookRow(
  book: ImportBook,
  flagTexts: FlagTexts,
  onToggle: () -> Unit,
  onRename: () -> Unit,
  onDetails: () -> Unit,
) {
  val kind = when (book.book.kind) {
    BookKind.SINGLE -> stringResource(CommonR.string.library_import_kind_single)
    BookKind.MULTI_PART -> stringResource(CommonR.string.library_import_kind_multi)
    BookKind.MIXED -> stringResource(CommonR.string.library_import_kind_mixed)
  }
  val order = when {
    book.usePlaylist && book.plan.usable -> stringResource(CommonR.string.library_order_playlist)
    book.sort.key == ChapterSortKey.MAGIC -> stringResource(CommonR.string.library_order_magic)
    book.sort.grouping == ChapterGrouping.BY_FOLDER -> stringResource(CommonR.string.library_order_folders)
    else -> stringResource(CommonR.string.library_order_flat)
  }
  val subtitle = listOf(
    stringResource(CommonR.string.library_import_files, book.book.fileCount),
    kind,
    stringResource(CommonR.string.library_import_order) + ": " + order,
  ).joinToString(" · ")

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onDetails)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      checked = book.selected,
      onCheckedChange = { onToggle() },
    )
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
    ) {
      Text(
        text = book.name,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (BookFlag.FEW_FILES in book.book.flags) WarningLine(flagTexts.few)
      if (BookFlag.MANY_FILES in book.book.flags) WarningLine(flagTexts.many)
      if (BookFlag.MIXED_STRUCTURE in book.book.flags) WarningLine(flagTexts.mixed)
      if (BookFlag.LOOKS_LIKE_PART in book.book.flags) WarningLine(flagTexts.part)
      if (book.plan.defective) WarningLine(flagTexts.playlistDefective)
    }
    IconButton(onClick = onRename) {
      Icon(
        imageVector = Icons.Rounded.Edit,
        contentDescription = stringResource(CommonR.string.library_rename_title),
      )
    }
  }
}

@Composable
private fun WarningLine(text: String) {
  Row(
    modifier = Modifier.padding(top = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(14.dp),
      imageVector = Icons.Rounded.Warning,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.error,
    )
    Text(
      modifier = Modifier.padding(start = 4.dp),
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.error,
    )
  }
}

@Composable
private fun RenameDialog(
  initial: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var text by remember { mutableStateOf(initial) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(CommonR.string.library_rename_title)) },
    text = {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = text,
        onValueChange = { text = it },
      )
    },
    confirmButton = {
      TextButton(
        enabled = text.isNotBlank(),
        onClick = { onConfirm(text.trim()) },
      ) {
        Text(text = stringResource(CommonR.string.sort_order_apply))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(CommonR.string.sort_order_cancel))
      }
    },
  )
}

/** Reihenfolge eines Buchs aendern, mit Live-Vorschau. Eine vorhandene Playlist (m3u) kann ersetzt werden. */
@Composable
private fun OrderDialog(
  book: ImportBook,
  onApply: (usePlaylist: Boolean, sort: ChapterSortSettings) -> Unit,
  onDismiss: () -> Unit,
) {
  var usePlaylist by remember { mutableStateOf(book.usePlaylist) }
  var groupByFolder by remember { mutableStateOf(book.sort.grouping == ChapterGrouping.BY_FOLDER) }
  var key by remember {
    mutableStateOf(
      book.sort.key.takeIf {
        it == ChapterSortKey.FILENAME || it == ChapterSortKey.DATE || it == ChapterSortKey.MAGIC
      } ?: ChapterSortKey.FILENAME,
    )
  }
  var reversed by remember { mutableStateOf(book.sort.reversed) }

  val sort = ChapterSortSettings(
    grouping = if (groupByFolder) ChapterGrouping.BY_FOLDER else ChapterGrouping.FLAT,
    key = key,
    reversed = reversed,
  )
  val playlistActive = usePlaylist && book.plan.usable
  val preview = remember(book.id, usePlaylist, sort) {
    LibraryPreview.entries(book.book.dir, book.plan, usePlaylist, sort)
  }
  val notListed = stringResource(CommonR.string.library_not_in_playlist)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = book.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
    confirmButton = {
      TextButton(onClick = { onApply(playlistActive, sort) }) {
        Text(text = stringResource(CommonR.string.sort_order_apply))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(CommonR.string.sort_order_cancel))
      }
    },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (book.plan.usable || book.plan.defective) {
          SwitchLine(
            text = stringResource(CommonR.string.library_use_playlist),
            checked = playlistActive,
            enabled = book.plan.usable,
            onCheckedChange = { usePlaylist = it },
          )
          if (book.plan.usable) {
            Text(
              text = stringResource(
                CommonR.string.library_playlist_info,
                book.plan.playlistNames.joinToString(", "),
                book.plan.matchedEntries,
                book.plan.entryCount,
                book.plan.unlistedFiles,
              ),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          } else {
            Text(
              text = stringResource(CommonR.string.library_flag_playlist_defective),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
            )
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        SwitchLine(
          text = stringResource(CommonR.string.sort_order_group_by_folder),
          checked = groupByFolder,
          enabled = !playlistActive,
          onCheckedChange = { groupByFolder = it },
        )
        Text(
          modifier = Modifier.padding(top = 8.dp),
          text = stringResource(CommonR.string.sort_order_sort_by),
          style = MaterialTheme.typography.labelLarge,
        )
        KeyLine(
          text = stringResource(CommonR.string.sort_order_key_filename),
          selected = key == ChapterSortKey.FILENAME,
          enabled = !playlistActive,
          onClick = { key = ChapterSortKey.FILENAME },
        )
        KeyLine(
          text = stringResource(CommonR.string.sort_order_key_date),
          selected = key == ChapterSortKey.DATE,
          enabled = !playlistActive,
          onClick = { key = ChapterSortKey.DATE },
        )
        KeyLine(
          text = stringResource(CommonR.string.sort_order_key_magic),
          selected = key == ChapterSortKey.MAGIC,
          enabled = !playlistActive,
          onClick = { key = ChapterSortKey.MAGIC },
        )
        SwitchLine(
          text = stringResource(CommonR.string.sort_order_reversed),
          checked = reversed,
          enabled = !playlistActive,
          onCheckedChange = { reversed = it },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
          modifier = Modifier.padding(bottom = 4.dp),
          text = stringResource(CommonR.string.sort_order_preview, preview.size),
          style = MaterialTheme.typography.labelLarge,
        )
        // Lange Dateinamen werden nicht abgeschnitten: die Liste laesst sich seitlich wischen.
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
          preview.take(MAX_PREVIEW_ROWS).forEachIndexed { index, entry ->
            val missing = playlistActive && !book.plan.rankByKey.containsKey(entry.item.file.key)
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
              Text(
                modifier = Modifier.width(40.dp),
                text = "${index + 1}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Column {
                Text(
                  text = entry.fileName,
                  style = MaterialTheme.typography.bodyMedium,
                  softWrap = false,
                )
                val detailLine = listOfNotNull(
                  entry.relativeFolder.takeIf { it.isNotEmpty() },
                  notListed.takeIf { missing },
                ).joinToString(" · ")
                if (detailLine.isNotEmpty()) {
                  Text(
                    text = detailLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (missing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                  )
                }
              }
            }
          }
        }
        if (preview.size > MAX_PREVIEW_ROWS) {
          Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(CommonR.string.sort_order_preview_more, preview.size - MAX_PREVIEW_ROWS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
  )
}

@Composable
private fun SwitchLine(
  text: String,
  checked: Boolean,
  enabled: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onCheckedChange(!checked) }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      style = MaterialTheme.typography.bodyLarge,
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
    )
  }
}

@Composable
private fun KeyLine(
  text: String,
  selected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled, onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      selected = selected,
      onClick = onClick,
      enabled = enabled,
    )
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}
