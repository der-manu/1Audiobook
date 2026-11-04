package voice.data.library

import voice.data.ChapterGrouping
import voice.data.ChapterPreviewEntry
import voice.data.ChapterSortItem
import voice.data.ChapterSortKey
import voice.data.ChapterSortSettings
import voice.data.ChapterSorter

/** Reihenfolge-Vorschau eines Buchs vor dem Import und die Einstellungen, die danach gespeichert werden. */
object LibraryPreview {

  /** Die Sortierung, die fuer die Vorschau gilt: Playlist, wenn gewaehlt und brauchbar, sonst [sort]. */
  fun effectiveSettings(
    plan: PlaylistPlan,
    usePlaylist: Boolean,
    sort: ChapterSortSettings,
  ): ChapterSortSettings {
    if (!usePlaylist || !plan.usable) return sort
    val grouping = if (plan.mode == PlaylistMode.GLOBAL) ChapterGrouping.FLAT else ChapterGrouping.BY_FOLDER
    return ChapterSortSettings(grouping = grouping, key = ChapterSortKey.PLAYLIST, reversed = false)
  }

  /** Was beim Import fuer das Buch gespeichert wird. Der Scanner leitet die Gruppierung der Playlist selbst ab. */
  fun settingsToStore(
    plan: PlaylistPlan,
    usePlaylist: Boolean,
    sort: ChapterSortSettings,
  ): ChapterSortSettings {
    return if (usePlaylist && plan.usable) sort.copy(key = ChapterSortKey.PLAYLIST, reversed = false) else sort
  }

  /**
   * Standard: nach Ordnern, dann nach Dateiname. Stehen CD-Nummern in den Dateinamen und ordnet der Namensvergleich
   * sie deshalb falsch, schlaegt "Zaubern" eine bessere Reihenfolge vor. Sonst bleibt alles wie gewohnt.
   */
  fun suggestedSort(dir: LibraryDir): ChapterSortSettings {
    val byName = ChapterSortSettings(ChapterGrouping.BY_FOLDER, ChapterSortKey.FILENAME, reversed = false)
    val magic = byName.copy(key = ChapterSortKey.MAGIC)
    fun order(settings: ChapterSortSettings) =
      entries(dir, PlaylistPlan.NONE, usePlaylist = false, sort = settings).map { it.item.file.key }
    return if (order(magic) != order(byName)) magic else byName
  }

  fun entries(
    dir: LibraryDir,
    plan: PlaylistPlan,
    usePlaylist: Boolean,
    sort: ChapterSortSettings,
  ): List<ChapterPreviewEntry<PlacedFile>> {
    val ranks = if (usePlaylist && plan.usable) plan.rankByKey else emptyMap()
    val items = dir.placedAudioFiles().map { placed ->
      ChapterSortItem(
        item = placed,
        folders = placed.folders,
        fileName = placed.file.name,
        title = null,
        lastModifiedMs = placed.file.lastModified,
        rank = ranks[placed.file.key],
      )
    }
    return ChapterSorter.toPreview(ChapterSorter.sort(items, effectiveSettings(plan, usePlaylist, sort)))
  }
}
