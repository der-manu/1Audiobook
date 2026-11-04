package voice.folderPicker.libraryImport

import voice.data.ChapterSortSettings
import voice.data.library.DetectedBook
import voice.data.library.PlaylistPlan

/** Ein erkanntes Buch in der Import-Vorschau samt allem, was der Nutzer daran aendern kann. */
internal data class ImportBook(
  val book: DetectedBook,
  val plan: PlaylistPlan,
  val name: String,
  val selected: Boolean,
  val usePlaylist: Boolean,
  /** Sortierung, die gilt, wenn keine Playlist verwendet wird. */
  val sort: ChapterSortSettings,
) {
  val id: String get() = book.dir.key

  /** Soll der Nutzer sich dieses Buch ansehen? */
  val needsReview: Boolean get() = book.needsReview || plan.defective
}

internal data class LibraryImportState(
  val scanning: Boolean = true,
  val scannedFiles: Int = 0,
  val books: List<ImportBook> = emptyList(),
  val looseRootFiles: Int = 0,
  val onlyReview: Boolean = false,
  val saving: Boolean = false,
  val paddings: String = "0;0;0;0",
) {
  val selectedCount: Int get() = books.count { it.selected }
  val reviewCount: Int get() = books.count { it.needsReview }
  val visibleBooks: List<ImportBook> get() = if (onlyReview) books.filter { it.needsReview } else books
}
