package voice.app.scanner

import voice.data.Chapter
import voice.data.ChapterGrouping
import voice.data.ChapterSortSettings
import voice.data.library.LibraryScanner
import voice.data.library.PlaylistMode
import voice.data.library.PlaylistPlanner
import voice.data.library.PlaylistReader
import voice.data.sortChapters
import voice.documentfile.CachedDocumentFile
import javax.inject.Inject

/**
 * Ordnet die Kapitel eines Buchs nach seiner m3u-Playlist. Ist sie nicht (mehr) brauchbar, etwa weil
 * die Datei fehlt oder defekt ist, gilt die Sortierung nach Ordnern und Dateinamen.
 */
class PlaylistChapterOrder
@Inject constructor(
  private val libraryScanner: LibraryScanner,
  private val playlistReader: PlaylistReader,
) {

  fun order(
    book: CachedDocumentFile,
    chapters: List<Chapter>,
    settings: ChapterSortSettings,
  ): List<Chapter> {
    val plan = PlaylistPlanner.plan(libraryScanner.scan(book), playlistReader::read)
    val grouping = if (plan.mode == PlaylistMode.GLOBAL) ChapterGrouping.FLAT else ChapterGrouping.BY_FOLDER
    return sortChapters(chapters, settings.copy(grouping = grouping), plan.rankByKey)
  }
}
