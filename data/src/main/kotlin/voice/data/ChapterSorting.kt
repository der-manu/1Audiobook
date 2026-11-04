package voice.data

/**
 * Verbindet die reine Sortierlogik [ChapterSorter] mit den echten [Chapter]-Objekten.
 */
fun Chapter.toSortItem(rank: Int? = null): ChapterSortItem<Chapter> {
  val segments = id.toUri().pathSegments.flatMap { it.split("/") }
  return ChapterSortItem(
    item = this,
    folders = segments.dropLast(1),
    fileName = segments.lastOrNull().orEmpty(),
    title = name,
    lastModifiedMs = fileLastModified.toEpochMilli(),
    rank = rank,
  )
}

/**
 * Sortiert die Kapitel. Bei `settings == null` gilt die bisherige Standard-Reihenfolge (Pfad, natuerlich).
 */
fun sortChapters(
  chapters: List<Chapter>,
  settings: ChapterSortSettings?,
  /** Playlist-Rang je Kapitel-ID (nur fuer [ChapterSortKey.PLAYLIST]). */
  ranks: Map<String, Int> = emptyMap(),
): List<Chapter> {
  if (settings == null) return chapters.sorted()
  return ChapterSorter.sort(chapters.map { it.toSortItem(ranks[it.id.value]) }, settings).map { it.item }
}

/**
 * Wie [sortChapters], aber mit den Angaben, die die Vorschau im Dialog braucht.
 */
fun previewChapters(
  chapters: List<Chapter>,
  settings: ChapterSortSettings?,
): List<ChapterPreviewEntry<Chapter>> {
  val items = chapters.map { it.toSortItem() }
  val sorted = if (settings == null) {
    val byId = items.associateBy { it.item.id }
    chapters.sorted().mapNotNull { byId[it.id] }
  } else {
    ChapterSorter.sort(items, settings)
  }
  return ChapterSorter.toPreview(sorted)
}
