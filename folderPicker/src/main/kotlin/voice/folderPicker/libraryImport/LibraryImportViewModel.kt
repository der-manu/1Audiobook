package voice.folderPicker.libraryImport

import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.olshevski.navigation.reimagined.popUpTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import voice.common.BookId
import voice.common.DispatcherProvider
import voice.common.comparator.NaturalOrderComparator
import voice.common.navigation.Destination
import voice.common.navigation.Destination.SelectFolderType.Mode
import voice.common.navigation.Navigator
import voice.common.pref.PrefKeys
import voice.data.ChapterSortSettings
import voice.data.folders.AudiobookFolders
import voice.data.library.BookDetector
import voice.data.library.LibraryPreview
import voice.data.library.LibraryScanner
import voice.data.library.NameCleaner
import voice.data.library.PlaylistPlanner
import voice.data.library.PlaylistReader
import voice.data.repo.BookNameStore
import voice.data.repo.ChapterSortStore
import voice.documentfile.CachedDocumentFileFactory
import voice.logging.core.Logger
import voice.pref.Pref
import javax.inject.Named

/**
 * Liest einen Ordnerbaum, erkennt die Buecher darin und laesst den Nutzer das Ergebnis pruefen und aendern.
 * Erst [confirm] speichert etwas.
 */
class LibraryImportViewModel
@AssistedInject constructor(
  private val dispatcherProvider: DispatcherProvider,
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  private val documentFileFactory: CachedDocumentFileFactory,
  private val libraryScanner: LibraryScanner,
  private val playlistReader: PlaylistReader,
  private val chapterSortStore: ChapterSortStore,
  private val bookNameStore: BookNameStore,
  @Named(PrefKeys.PADDING)
  private val paddingPref: Pref<String>,
  @Assisted
  private val uri: Uri,
  @Assisted
  private val mode: Mode,
) {

  private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.main)
  private val state = mutableStateOf(LibraryImportState())
  private var started = false

  @Composable
  internal fun viewState(): LibraryImportState {
    LaunchedEffect(Unit) { start() }
    return state.value.copy(paddings = paddingPref.value)
  }

  /** Der Scan laeuft im eigenen Scope, damit eine Drehung des Bildschirms ihn nicht abbricht. */
  private fun start() {
    if (started) return
    started = true
    scope.launch { scan() }
  }

  private suspend fun scan() {
    try {
      withContext(dispatcherProvider.io) {
        val rootUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        var files = 0
        val tree = libraryScanner.scan(documentFileFactory.create(rootUri)) {
          files++
          if (files % 200 == 0) state.value = state.value.copy(scannedFiles = files)
        }
        val detection = BookDetector.detect(tree)
        val books = detection.books
          .map { detected ->
            val plan = PlaylistPlanner.plan(detected.dir, playlistReader::read)
            ImportBook(
              book = detected,
              plan = plan,
              name = NameCleaner.bookName(detected.pathNames),
              selected = true,
              usePlaylist = plan.usable,
              sort = LibraryPreview.suggestedSort(detected.dir),
            )
          }
          .sortedWith(compareBy(NaturalOrderComparator.stringComparator) { it.book.pathNames.joinToString("/") })
        state.value = state.value.copy(
          scanning = false,
          scannedFiles = files,
          books = books,
          looseRootFiles = detection.looseRootFiles,
        )
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(e, "Library scan failed")
      state.value = state.value.copy(scanning = false)
    }
  }

  private fun updateBook(
    id: String,
    transform: (ImportBook) -> ImportBook,
  ) {
    state.value = state.value.copy(books = state.value.books.map { if (it.id == id) transform(it) else it })
  }

  internal fun toggle(id: String) = updateBook(id) { it.copy(selected = !it.selected) }

  /** Wirkt nur auf die gerade sichtbaren Buecher. */
  internal fun setAllSelected(selected: Boolean) {
    val visible = state.value.visibleBooks.map { it.id }.toSet()
    state.value = state.value.copy(
      books = state.value.books.map { if (it.id in visible) it.copy(selected = selected) else it },
    )
  }

  internal fun rename(id: String, name: String) = updateBook(id) { it.copy(name = name) }

  internal fun setOrder(
    id: String,
    usePlaylist: Boolean,
    sort: ChapterSortSettings,
  ) = updateBook(id) { it.copy(usePlaylist = usePlaylist && it.plan.usable, sort = sort) }

  internal fun setOnlyReview(onlyReview: Boolean) {
    state.value = state.value.copy(onlyReview = onlyReview)
  }

  internal fun cancel() {
    navigator.goBack()
  }

  internal fun confirm() {
    val current = state.value
    if (current.saving || current.scanning) return
    state.value = current.copy(saving = true)
    scope.launch {
      try {
        withContext(dispatcherProvider.io) {
          val selected = current.books.filter { it.selected }
          // Reihenfolge und Titel zuerst merken, damit der Scanner sie schon beim Anlegen der Buecher kennt.
          selected.forEach { book ->
            chapterSortStore.set(BookId(book.id), LibraryPreview.settingsToStore(book.plan, book.usePlaylist, book.sort))
          }
          bookNameStore.setAll(selected.associate { BookId(it.id) to it.name })
          audiobookFolders.setLibrary(uri, selected.map { Uri.parse(it.id) })
        }
        finish()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Logger.e(e, "Could not add library")
        state.value = state.value.copy(saving = false)
      }
    }
  }

  private fun finish() {
    when (mode) {
      Mode.Default -> {
        navigator.execute {
          popUpTo { it is Destination.FolderPicker }
        }
      }
      Mode.Onboarding -> {
        navigator.goTo(
          destination = Destination.OnboardingCompletion,
          replace = true,
        )
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      uri: Uri,
      mode: Mode,
    ): LibraryImportViewModel
  }
}
