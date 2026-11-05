package voice.cover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.common.R as CommonR

/**
 * Hilfen unter der Suchleiste: kuerzere Suchbegriffe zum Antippen, die Suche im Browser und das Einfuegen eines
 * Bild-Links. Die beiden letzten funktionieren auch dann, wenn die automatische Suche einmal ausfaellt.
 */
@Composable
internal fun CoverActions(
  suggestions: List<String>,
  onSuggestion: (String) -> Unit,
  onOpenBrowser: () -> Unit,
  onUseLink: (String) -> Unit,
) {
  var showLinkDialog by remember { mutableStateOf(false) }

  Column {
    if (suggestions.isNotEmpty()) {
      LazyRow(
        modifier = Modifier.padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(suggestions) { suggestion ->
          SuggestionChip(
            onClick = { onSuggestion(suggestion) },
            label = { Text(text = suggestion, maxLines = 1) },
          )
        }
      }
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
    ) {
      TextButton(onClick = onOpenBrowser) {
        Icon(
          modifier = Modifier.size(18.dp),
          imageVector = Icons.Rounded.OpenInBrowser,
          contentDescription = null,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(id = CommonR.string.cover_open_browser))
      }
      TextButton(onClick = { showLinkDialog = true }) {
        Icon(
          modifier = Modifier.size(18.dp),
          imageVector = Icons.Rounded.Link,
          contentDescription = null,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(id = CommonR.string.cover_use_link))
      }
    }
  }

  if (showLinkDialog) {
    LinkDialog(
      onConfirm = { link ->
        showLinkDialog = false
        onUseLink(link)
      },
      onDismiss = { showLinkDialog = false },
    )
  }
}

@Composable
private fun LinkDialog(
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var link by remember { mutableStateOf("") }
  val trimmed = link.trim()
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(id = CommonR.string.cover_link_title)) },
    text = {
      Column {
        Text(
          text = stringResource(id = CommonR.string.cover_link_hint),
          style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          value = link,
          onValueChange = { link = it },
          singleLine = true,
          placeholder = { Text(text = "https://") },
        )
      }
    },
    confirmButton = {
      TextButton(
        enabled = trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true),
        onClick = { onConfirm(trimmed) },
      ) {
        Text(text = stringResource(id = CommonR.string.cover_link_use))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = CommonR.string.sort_order_cancel))
      }
    },
  )
}
