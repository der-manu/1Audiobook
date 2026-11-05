package voice.cover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.cover.api.CoverFailure
import voice.cover.api.SearchResponse
import voice.common.R as CommonR
import voice.strings.R

@Composable
internal fun CoverContents(
  viewState: SelectCoverFromInternetViewModel.ViewState,
  onCoverClick: (SearchResponse.ImageResult) -> Unit,
  onRetry: () -> Unit,
  onOpenBrowser: () -> Unit,
) {
  when (viewState) {
    is SelectCoverFromInternetViewModel.ViewState.Content -> {
      ItemsContent(viewState, onCoverClick)
    }
    is SelectCoverFromInternetViewModel.ViewState.Loading -> {
      LoadingContent()
    }
    is SelectCoverFromInternetViewModel.ViewState.Error -> {
      MessageContent(
        title = stringResource(id = R.string.generic_error_message),
        message = failureText(viewState.failure),
        onRetry = onRetry,
        onOpenBrowser = onOpenBrowser,
      )
    }
    is SelectCoverFromInternetViewModel.ViewState.Empty -> {
      MessageContent(
        title = stringResource(id = CommonR.string.cover_empty_title),
        message = stringResource(CommonR.string.cover_empty_message, viewState.query),
        onRetry = null,
        onOpenBrowser = onOpenBrowser,
      )
    }
  }
}

/** Sagt dem Nutzer, was schiefging, statt nur "Fehler" zu zeigen. */
@Composable
private fun failureText(failure: CoverFailure): String {
  return when (failure.kind) {
    CoverFailure.Kind.HTTP -> stringResource(CommonR.string.cover_error_blocked, failure.httpCode)
    CoverFailure.Kind.OFFLINE -> stringResource(CommonR.string.cover_error_offline)
    CoverFailure.Kind.UNEXPECTED -> stringResource(CommonR.string.cover_error_unexpected)
  }
}

@Composable
private fun MessageContent(
  title: String,
  message: String,
  onRetry: (() -> Unit)?,
  onOpenBrowser: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.size(12.dp))
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.size(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      if (onRetry != null) {
        Button(onClick = onRetry) {
          Text(text = stringResource(id = R.string.generic_error_retry))
        }
      }
      OutlinedButton(onClick = onOpenBrowser) {
        Text(text = stringResource(id = CommonR.string.cover_open_browser))
      }
    }
  }
}

@Composable
private fun LoadingContent() {
  Box(
    modifier = Modifier
      .fillMaxSize(),
  ) {
    CircularProgressIndicator(Modifier.align(Alignment.Center))
  }
}

/** Liegt ueber dem Inhalt, solange ein gewaehltes Bild geladen wird, und faengt weitere Klicks ab. */
@Composable
internal fun DownloadingOverlay() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.5f))
      .clickable(onClick = {}),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator()
      Spacer(modifier = Modifier.size(12.dp))
      Text(
        text = stringResource(id = CommonR.string.cover_downloading),
        color = Color.White,
      )
    }
  }
}

@Composable
private fun ItemsContent(
  viewState: SelectCoverFromInternetViewModel.ViewState.Content,
  onCoverClick: (SearchResponse.ImageResult) -> Unit,
) {
  val items = viewState.items
  LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
    contentPadding = PaddingValues(top = 8.dp),
    content = {
      items(
        count = items.itemCount,
        key = null,
      ) { index ->
        val item = items[index]
        if (item != null) {
          CoverImage(onCoverClick = onCoverClick, item = item)
        }
      }
    },
  )
}

@Composable
private fun CoverImage(
  item: SearchResponse.ImageResult,
  onCoverClick: (SearchResponse.ImageResult) -> Unit,
) {
  // Fehlen die Masse in der Antwort, bleibt das Bild quadratisch, statt durch null zu teilen.
  val ratio = if (item.width > 0 && item.height > 0) item.width.toFloat() / item.height else 1f
  AsyncImage(
    modifier = Modifier
      .clickable {
        onCoverClick(item)
      }
      .fillMaxWidth()
      .aspectRatio(ratio),
    model = item.thumbnail,
    contentDescription = null,
    placeholder = ColorPainter(MaterialTheme.colorScheme.onSurfaceVariant),
  )
}
