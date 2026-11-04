package voice.data.library

import io.kotest.matchers.shouldBe
import org.junit.Test
import voice.data.ChapterGrouping
import voice.data.ChapterSortKey
import voice.data.ChapterSortSettings

class LibraryPreviewTest {

  private val folderSort = ChapterSortSettings(ChapterGrouping.BY_FOLDER, ChapterSortKey.FILENAME, false)

  private val book = dir(
    "Buch",
    *f("liste.m3u"),
    d(dir("CD1", *f("01.mp3", "02.mp3"), key = "Buch/CD1")),
    d(dir("CD2", *f("01.mp3", "02.mp3"), key = "Buch/CD2")),
    key = "Buch",
  )

  private fun names(entries: List<voice.data.ChapterPreviewEntry<PlacedFile>>) = entries.map { it.item.relativePath }

  @Test
  fun withoutPlaylistTheChosenSortingDecides() {
    val entries = LibraryPreview.entries(book, PlaylistPlan.NONE, usePlaylist = false, sort = folderSort)
    names(entries) shouldBe listOf("CD1/01.mp3", "CD1/02.mp3", "CD2/01.mp3", "CD2/02.mp3")
    entries.map { it.relativeFolder } shouldBe listOf("CD1", "CD1", "CD2", "CD2")
  }

  @Test
  fun aUsablePlaylistReplacesTheSorting() {
    val plan = PlaylistPlanner.plan(book) { "CD2/02.mp3\nCD1/01.mp3\nCD2/01.mp3\nCD1/02.mp3".toByteArray() }
    plan.mode shouldBe PlaylistMode.GLOBAL
    val entries = LibraryPreview.entries(book, plan, usePlaylist = true, sort = folderSort)
    names(entries) shouldBe listOf("CD2/02.mp3", "CD1/01.mp3", "CD2/01.mp3", "CD1/02.mp3")
  }

  @Test
  fun theUserCanReplaceThePlaylistBySorting() {
    val plan = PlaylistPlanner.plan(book) { "CD2/02.mp3\nCD1/01.mp3\nCD2/01.mp3\nCD1/02.mp3".toByteArray() }
    val entries = LibraryPreview.entries(book, plan, usePlaylist = false, sort = folderSort)
    names(entries) shouldBe listOf("CD1/01.mp3", "CD1/02.mp3", "CD2/01.mp3", "CD2/02.mp3")
  }

  @Test
  fun filesMissingFromThePlaylistComeLast() {
    val plan = PlaylistPlanner.plan(book) { "CD2/01.mp3\nCD1/02.mp3\nCD1/01.mp3".toByteArray() }
    plan.unlistedFiles shouldBe 1
    val entries = LibraryPreview.entries(book, plan, usePlaylist = true, sort = folderSort)
    names(entries) shouldBe listOf("CD2/01.mp3", "CD1/02.mp3", "CD1/01.mp3", "CD2/02.mp3")
  }

  @Test
  fun perFolderPlaylistsKeepFoldersTogether() {
    val perDir = dir(
      "Buch",
      d(dir("CD1", *f("a.mp3", "b.mp3", "p.m3u"), key = "Buch/CD1")),
      d(dir("CD2", *f("c.mp3", "d.mp3"), key = "Buch/CD2")),
      key = "Buch",
    )
    val plan = PlaylistPlanner.plan(perDir) { "b.mp3\na.mp3".toByteArray() }
    val entries = LibraryPreview.entries(perDir, plan, usePlaylist = true, sort = folderSort)
    names(entries) shouldBe listOf("CD1/b.mp3", "CD1/a.mp3", "CD2/c.mp3", "CD2/d.mp3")
  }

  @Test
  fun effectiveSettingsUsePlaylistGroupingForTheMode() {
    val global = PlaylistPlan.NONE.copy(mode = PlaylistMode.GLOBAL)
    val perDir = PlaylistPlan.NONE.copy(mode = PlaylistMode.PER_DIR)
    LibraryPreview.effectiveSettings(global, true, folderSort).grouping shouldBe ChapterGrouping.FLAT
    LibraryPreview.effectiveSettings(perDir, true, folderSort).grouping shouldBe ChapterGrouping.BY_FOLDER
    LibraryPreview.effectiveSettings(global, true, folderSort).key shouldBe ChapterSortKey.PLAYLIST
    LibraryPreview.effectiveSettings(global, false, folderSort) shouldBe folderSort
    LibraryPreview.effectiveSettings(PlaylistPlan.NONE, true, folderSort) shouldBe folderSort
  }

  @Test
  fun storedSettingsOnlyUsePlaylistWhenChosenAndUsable() {
    val usable = PlaylistPlan.NONE.copy(mode = PlaylistMode.GLOBAL)
    LibraryPreview.settingsToStore(usable, true, folderSort).key shouldBe ChapterSortKey.PLAYLIST
    LibraryPreview.settingsToStore(usable, false, folderSort) shouldBe folderSort
    LibraryPreview.settingsToStore(PlaylistPlan.NONE, true, folderSort) shouldBe folderSort
  }

  @Test
  fun settingsWithPlaylistKeySurviveEncoding() {
    val settings = ChapterSortSettings(ChapterGrouping.BY_FOLDER, ChapterSortKey.PLAYLIST, false)
    ChapterSortSettings.decode(settings.encode()) shouldBe settings
  }
}
