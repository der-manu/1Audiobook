package voice.playbackScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import voice.common.R as CommonR
import voice.data.Chapter
import voice.data.ChapterGrouping
import voice.data.ChapterPreviewEntry
import voice.data.ChapterSortKey
import voice.data.ChapterSortSettings
import voice.data.previewChapters

/** So viele Zeilen zeigt die Vorschau maximal, damit auch Buecher mit sehr vielen Dateien fluessig bleiben. */
private const val MAX_PREVIEW_ROWS = 300

@Composable
internal fun SortOrderDialog(
  dialogState: BookPlayDialogViewState.SortOrderDialog,
  onApply: (ChapterSortSettings?) -> Unit,
  onDismiss: () -> Unit,
) {
  val current = dialogState.current
  var useDefault by remember { mutableStateOf(current == null) }
  var groupByFolder by remember {
    mutableStateOf((current?.grouping ?: ChapterGrouping.BY_FOLDER) == ChapterGrouping.BY_FOLDER)
  }
  var key by remember { mutableStateOf(current?.key ?: ChapterSortKey.FILENAME) }
  var reversed by remember { mutableStateOf(current?.reversed ?: false) }

  // null bedeutet: Standard-Reihenfolge wie bisher
  val settings: ChapterSortSettings? = if (useDefault) {
    null
  } else {
    ChapterSortSettings(
      grouping = if (groupByFolder) ChapterGrouping.BY_FOLDER else ChapterGrouping.FLAT,
      key = key,
      reversed = reversed,
    )
  }

  val preview = remember(dialogState.chapters, settings) {
    previewChapters(dialogState.chapters, settings)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = stringResource(id = CommonR.string.sort_order_title))
    },
    confirmButton = {
      TextButton(onClick = { onApply(settings) }) {
        Text(text = stringResource(id = CommonR.string.sort_order_apply))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = CommonR.string.sort_order_cancel))
      }
    },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SwitchRow(
          text = stringResource(id = CommonR.string.sort_order_default),
          checked = useDefault,
          onCheckedChange = { useDefault = it },
        )
        SwitchRow(
          text = stringResource(id = CommonR.string.sort_order_group_by_folder),
          hint = stringResource(id = CommonR.string.sort_order_group_by_folder_hint),
          checked = groupByFolder,
          enabled = !useDefault,
          onCheckedChange = { groupByFolder = it },
        )
        Text(
          text = stringResource(id = CommonR.string.sort_order_sort_by),
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.padding(top = 8.dp),
        )
        KeyOption(
          text = stringResource(id = CommonR.string.sort_order_key_filename),
          selected = key == ChapterSortKey.FILENAME,
          enabled = !useDefault,
          onClick = { key = ChapterSortKey.FILENAME },
        )
        KeyOption(
          text = stringResource(id = CommonR.string.sort_order_key_title),
          selected = key == ChapterSortKey.TITLE,
          enabled = !useDefault,
          onClick = { key = ChapterSortKey.TITLE },
        )
        KeyOption(
          text = stringResource(id = CommonR.string.sort_order_key_date),
          selected = key == ChapterSortKey.DATE,
          enabled = !useDefault,
          onClick = { key = ChapterSortKey.DATE },
        )
        SwitchRow(
          text = stringResource(id = CommonR.string.sort_order_reversed),
          checked = reversed,
          enabled = !useDefault,
          onCheckedChange = { reversed = it },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
          text = stringResource(CommonR.string.sort_order_preview, preview.size),
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.padding(bottom = 4.dp),
        )
        val showTitle = !useDefault && key == ChapterSortKey.TITLE
        preview.take(MAX_PREVIEW_ROWS).forEachIndexed { index, entry ->
          PreviewRow(index = index + 1, entry = entry, showTitle = showTitle)
        }
        if (preview.size > MAX_PREVIEW_ROWS) {
          Text(
            text = stringResource(CommonR.string.sort_order_preview_more, preview.size - MAX_PREVIEW_ROWS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }
    },
  )
}

@Composable
private fun SwitchRow(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  hint: String? = null,
  enabled: Boolean = true,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onCheckedChange(!checked) }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = text, style = MaterialTheme.typography.bodyLarge)
      if (hint != null) {
        Text(
          text = hint,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
    )
  }
}

@Composable
private fun KeyOption(
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

@Composable
private fun PreviewRow(
  index: Int,
  entry: ChapterPreviewEntry<Chapter>,
  showTitle: Boolean,
) {
  val primary = if (showTitle) {
    entry.title?.takeIf { it.isNotBlank() } ?: entry.fileName
  } else {
    entry.fileName
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
  ) {
    Text(
      text = "$index.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(40.dp),
    )
    Column {
      Text(
        text = primary,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (entry.relativeFolder.isNotEmpty()) {
        Text(
          text = entry.relativeFolder,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
