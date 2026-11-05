package voice.cover

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.squareup.anvil.annotations.ContributesTo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import voice.common.AppScope
import voice.common.BookId
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.cover.api.CoverApi
import voice.cover.api.CoverFailure
import voice.cover.api.ImageSearchPagingSource
import voice.cover.api.SearchResponse
import voice.data.repo.BookRepository
import voice.common.R as CommonR
import voice.strings.R as StringsR

class SelectCoverFromInternetViewModel
@AssistedInject constructor(
  private val api: CoverApi,
  private val bookRepository: BookRepository,
  private val navigator: Navigator,
  private val context: Context,
  private val coverDownloader: CoverDownloader,
  @Assisted private val bookId: BookId,
) {

  @Composable
  internal fun viewState(events: Flow<Events>): ViewState {
    var bookNameWithAuthor: BookNameWithAuthor? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
      val content = bookRepository.get(bookId)?.content
      bookNameWithAuthor = BookNameWithAuthor(
        bookName = content?.name ?: "",
        author = content?.author ?: "",
      )
    }
    val nameWithAuthor = bookNameWithAuthor ?: return ViewState.Loading("")

    var query: String by remember(nameWithAuthor) {
      val initial = if (nameWithAuthor.author == null) {
        context.getString(StringsR.string.cover_search_template_no_author, nameWithAuthor.bookName)
      } else {
        context.getString(
          StringsR.string.cover_search_template_with_author,
          nameWithAuthor.bookName,
          nameWithAuthor.author,
        )
      }
      mutableStateOf(initial)
    }
    var downloading by remember { mutableStateOf(false) }

    // Kuerzere Suchbegriffe als Auswahl: ein langer Titel samt Zusaetzen findet oft nichts, der reine Titel schon.
    val suggestions = remember(nameWithAuthor, query) {
      QuerySuggestions.build(nameWithAuthor.bookName, nameWithAuthor.author)
        .filterNot { it.equals(query, ignoreCase = true) }
    }

    val items = remember(query) {
      Pager(
        config = PagingConfig(10),
        pagingSourceFactory = {
          ImageSearchPagingSource(api, query)
        },
      ).flow
    }.collectAsLazyPagingItems()

    LaunchedEffect(events) {
      events.collect { event ->
        when (event) {
          is Events.Retry -> items.retry()
          is Events.CoverClick -> {
            // Das Original ist oft gesperrt (HTTP 403); dann zaehlt das kleinere Vorschaubild.
            downloadAndEdit(listOf(event.cover.image, event.cover.thumbnail)) { downloading = it }
          }
          is Events.UseLink -> {
            downloadAndEdit(listOf(event.url)) { downloading = it }
          }
          is Events.QueryChange -> {
            query = event.query
          }
          is Events.OpenInBrowser -> {
            navigator.goTo(Destination.Website(browserSearchUrl(query)))
          }
        }
      }
    }

    val refresh = items.loadState.refresh
    if (refresh is LoadState.Error) {
      return ViewState.Error(
        query = query,
        failure = CoverFailure.from(refresh.error),
        suggestions = suggestions,
        downloading = downloading,
      )
    }
    if (refresh is LoadState.Loading && items.itemCount == 0) {
      return ViewState.Loading(query = query, suggestions = suggestions, downloading = downloading)
    }
    if (refresh is LoadState.NotLoading && items.itemCount == 0 && query.isNotBlank()) {
      return ViewState.Empty(query = query, suggestions = suggestions, downloading = downloading)
    }
    return ViewState.Content(items = items, query = query, suggestions = suggestions, downloading = downloading)
  }

  private fun browserSearchUrl(query: String): String {
    return "https://duckduckgo.com/?q=${Uri.encode(query)}&iax=images&ia=images"
  }

  /** Probiert die Adressen der Reihe nach. Erst wenn eine ein echtes Bild liefert, geht es zum Bearbeiten weiter. */
  private suspend fun downloadAndEdit(
    urls: List<String>,
    setDownloading: (Boolean) -> Unit,
  ) {
    setDownloading(true)
    val downloaded = try {
      urls.firstNotNullOfOrNull { coverDownloader.download(it) }
    } finally {
      setDownloading(false)
    }
    if (downloaded != null) {
      navigator.goBack()
      navigator.goTo(Destination.EditCover(bookId, downloaded.toUri()))
    } else {
      Toast.makeText(context, CommonR.string.cover_download_failed, Toast.LENGTH_LONG).show()
    }
  }

  internal sealed interface ViewState {

    val query: String
    val suggestions: List<String>

    /** Wird gerade ein gewaehltes Bild geladen? Dann liegt ein Fortschritt ueber dem Inhalt. */
    val downloading: Boolean

    data class Loading(
      override val query: String,
      override val suggestions: List<String> = emptyList(),
      override val downloading: Boolean = false,
    ) : ViewState

    data class Error(
      override val query: String,
      val failure: CoverFailure = CoverFailure(CoverFailure.Kind.UNEXPECTED),
      override val suggestions: List<String> = emptyList(),
      override val downloading: Boolean = false,
    ) : ViewState

    /** Die Suche lief fehlerfrei, fand aber keine Bilder. */
    data class Empty(
      override val query: String,
      override val suggestions: List<String> = emptyList(),
      override val downloading: Boolean = false,
    ) : ViewState

    data class Content(
      val items: LazyPagingItems<SearchResponse.ImageResult>,
      override val query: String,
      override val suggestions: List<String> = emptyList(),
      override val downloading: Boolean = false,
    ) : ViewState
  }

  internal sealed interface Events {
    data object Retry : Events
    data object OpenInBrowser : Events
    data class CoverClick(val cover: SearchResponse.ImageResult) : Events
    data class UseLink(val url: String) : Events
    data class QueryChange(val query: String) : Events
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): SelectCoverFromInternetViewModel

    @ContributesTo(AppScope::class)
    interface Provider {
      val factory: Factory
    }
  }

  private data class BookNameWithAuthor(
    val bookName: String,
    val author: String?,
  )
}
