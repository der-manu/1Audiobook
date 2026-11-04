package voice.data

import voice.common.comparator.NaturalOrderComparator

/**
 * Eine Datei, auf ihre fuer die Sortierung relevanten Eigenschaften reduziert.
 *
 * @param folders Pfad-Segmente der Ordner (ohne Dateiname), von der Wurzel nach unten.
 */
data class ChapterSortItem<T>(
  val item: T,
  val folders: List<String>,
  val fileName: String,
  val title: String?,
  val lastModifiedMs: Long,
)

data class ChapterPreviewEntry<T>(
  val item: T,
  /** Ordner relativ zum gemeinsamen Elternordner aller Dateien, leer wenn direkt im Buchordner. */
  val relativeFolder: String,
  val fileName: String,
  val title: String?,
)

object ChapterSorter {

  private val natural: Comparator<String> = NaturalOrderComparator.stringComparator

  private fun String.baseName(): String = substringBeforeLast('.', this)

  /** Ordnerlisten Segment fuer Segment vergleichen. Ein Elternordner kommt vor seinen Unterordnern. */
  private val folderComparator = Comparator<List<String>> { a, b ->
    val common = minOf(a.size, b.size)
    for (i in 0 until common) {
      val result = natural.compare(a[i], b[i])
      if (result != 0) return@Comparator result
      val raw = a[i].compareTo(b[i])
      if (raw != 0) return@Comparator raw
    }
    a.size - b.size
  }

  fun <T> sort(
    items: List<ChapterSortItem<T>>,
    settings: ChapterSortSettings,
  ): List<ChapterSortItem<T>> {
    val byFolder = Comparator<ChapterSortItem<T>> { a, b ->
      folderComparator.compare(a.folders, b.folders)
    }
    val byFileName = Comparator<ChapterSortItem<T>> { a, b ->
      natural.compare(a.fileName.baseName(), b.fileName.baseName())
    }
    val byRawFileName = Comparator<ChapterSortItem<T>> { a, b ->
      a.fileName.compareTo(b.fileName)
    }
    val byKey: Comparator<ChapterSortItem<T>> = when (settings.key) {
      ChapterSortKey.FILENAME -> byFileName
      ChapterSortKey.TITLE -> Comparator { a, b ->
        natural.compare(a.title ?: a.fileName.baseName(), b.title ?: b.fileName.baseName())
      }
      ChapterSortKey.DATE -> Comparator { a, b ->
        a.lastModifiedMs.compareTo(b.lastModifiedMs)
      }
    }

    val comparator: Comparator<ChapterSortItem<T>> = when (settings.grouping) {
      ChapterGrouping.BY_FOLDER -> byFolder.then(byKey).then(byFileName).then(byRawFileName)
      ChapterGrouping.FLAT -> byKey.then(byFileName).then(byFolder).then(byRawFileName)
    }

    val sorted = items.sortedWith(comparator)
    return if (settings.reversed) sorted.asReversed().toList() else sorted
  }

  /**
   * Bereitet eine bereits sortierte Liste fuer die Vorschau auf.
   * Die Ordner werden relativ zum gemeinsamen Elternordner aller Dateien angezeigt.
   */
  fun <T> toPreview(sorted: List<ChapterSortItem<T>>): List<ChapterPreviewEntry<T>> {
    val prefix = commonPrefixLength(sorted.map { it.folders })
    return sorted.map {
      ChapterPreviewEntry(
        item = it.item,
        relativeFolder = it.folders.drop(prefix).joinToString("/"),
        fileName = it.fileName,
        title = it.title,
      )
    }
  }

  private fun commonPrefixLength(lists: List<List<String>>): Int {
    val first = lists.firstOrNull() ?: return 0
    var length = first.size
    for (list in lists) {
      var i = 0
      while (i < length && i < list.size && list[i] == first[i]) i++
      length = i
    }
    return length
  }
}
