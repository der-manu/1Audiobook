package voice.data.library

import io.kotest.matchers.shouldBe
import org.junit.Test

class PlaylistTest {

  private fun placed(vararg paths: String): List<PlacedFile> = paths.map { path ->
    val parts = path.split("/")
    PlacedFile(LibraryFile(parts.last(), path), parts.dropLast(1))
  }

  private fun ranksByName(match: PlaylistMatch) = match.rankByKey.entries.sortedBy { it.value }.map { it.key }

  @Test
  fun parserSkipsCommentsAndBlankLines() {
    val text = "#EXTM3U\n#EXTINF:123,Kapitel 1\n01 Intro.mp3\n\n#EXTINF:5,x\r\n02 Weiter.mp3\r\n"
    PlaylistParser.entries(text) shouldBe listOf("01 Intro.mp3", "02 Weiter.mp3")
  }

  @Test
  fun parserDecodesUtf8WithBomAndFallsBackToWindows1252() {
    val utf8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "Märchen.mp3".toByteArray(Charsets.UTF_8)
    PlaylistParser.decode(utf8) shouldBe "Märchen.mp3"
    // 0xE4 ist in Windows-1252 ein "ä", als UTF-8 aber ungueltig
    val ansi = byteArrayOf('M'.code.toByte(), 0xE4.toByte(), 'r'.code.toByte(), 'c'.code.toByte(), 'h'.code.toByte(), 'e'.code.toByte(), 'n'.code.toByte())
    PlaylistParser.decode(ansi) shouldBe "Märchen"
  }

  @Test
  fun parserDecodesFileUrls() {
    PlaylistParser.entries("file:///C:/Hoerbuch/Teil%201/01%20Anfang.mp3") shouldBe listOf("C:/Hoerbuch/Teil 1/01 Anfang.mp3")
  }

  @Test
  fun matchesRelativeWindowsAndAbsolutePathsByFileName() {
    val files = placed("CD1/01.mp3", "CD1/02.mp3", "CD2/01.mp3")
    val match = PlaylistMatcher.match(
      listOf("""C:\Musik\Buch\CD2\01.mp3""", "CD1\\02.mp3", "./CD1/01.mp3"),
      files,
    )
    match.matchedEntries shouldBe 3
    ranksByName(match) shouldBe listOf("CD2/01.mp3", "CD1/02.mp3", "CD1/01.mp3")
  }

  @Test
  fun ignoresCaseUmlautsAndExtensionDifferences() {
    val files = placed("Maerchen 01.mp3", "ÄPFEL 02.MP3", "Titel 03.mp3")
    val match = PlaylistMatcher.match(listOf("märchen 01.mp3", "Aepfel 02.mp3", "Titel 03.wav"), files)
    match.matchedEntries shouldBe 3
    ranksByName(match) shouldBe listOf("Maerchen 01.mp3", "ÄPFEL 02.MP3", "Titel 03.mp3")
  }

  @Test
  fun everyFileIsMatchedAtMostOnce() {
    val files = placed("a/01.mp3", "b/01.mp3")
    val match = PlaylistMatcher.match(listOf("01.mp3", "01.mp3", "01.mp3"), files)
    match.matchedEntries shouldBe 2
    match.unmatchedEntries.size shouldBe 1
  }

  @Test
  fun missingFilesAreReported() {
    val match = PlaylistMatcher.match(listOf("a.mp3", "weg.mp3"), placed("a.mp3"))
    match.matchedEntries shouldBe 1
    match.unmatchedEntries shouldBe listOf("weg.mp3")
  }

  private fun bytes(vararg lines: String) = lines.joinToString("\n").toByteArray()

  @Test
  fun planUsesAPlaylistInTheBookFolderForAllFiles() {
    val book = dir(
      "Buch",
      *f("liste.m3u"),
      d(dir("CD1", *f("a.mp3", "b.mp3"), key = "Buch/CD1")),
      d(dir("CD2", *f("c.mp3"), key = "Buch/CD2")),
      key = "Buch",
    )
    val plan = PlaylistPlanner.plan(book) { bytes("CD2/c.mp3", "CD1/b.mp3", "CD1/a.mp3") }
    plan.mode shouldBe PlaylistMode.GLOBAL
    plan.rankByKey.entries.sortedBy { it.value }.map { it.key } shouldBe listOf("Buch/CD2/c.mp3", "Buch/CD1/b.mp3", "Buch/CD1/a.mp3")
    plan.unlistedFiles shouldBe 0
  }

  @Test
  fun planCountsFilesMissingFromThePlaylist() {
    val book = dir("Buch", *f("liste.m3u", "a.mp3", "b.mp3", "c.mp3"), key = "Buch")
    val plan = PlaylistPlanner.plan(book) { bytes("b.mp3", "a.mp3") }
    plan.mode shouldBe PlaylistMode.GLOBAL
    plan.unlistedFiles shouldBe 1
  }

  @Test
  fun planIsDefectiveWhenMostEntriesPointToNothing() {
    val book = dir("Buch", *f("liste.m3u", "a.mp3", "b.mp3"), key = "Buch")
    val plan = PlaylistPlanner.plan(book) { bytes("x1.mp3", "x2.mp3", "x3.mp3", "a.mp3") }
    plan.mode shouldBe PlaylistMode.NONE
    plan.defective shouldBe true
  }

  @Test
  fun planRejectsAPlaylistThatCoversOnlyAFractionOfTheFiles() {
    val files = (1..10).map { "%02d.mp3".format(it) }.toTypedArray()
    val book = dir("Buch", *f("liste.m3u", *files), key = "Buch")
    val plan = PlaylistPlanner.plan(book) { bytes("01.mp3", "02.mp3") }
    plan.mode shouldBe PlaylistMode.NONE
    plan.defective shouldBe true
  }

  @Test
  fun planFallsBackToPlaylistsInsideDiscFolders() {
    val book = dir(
      "Buch",
      d(dir("CD1", *f("a.mp3", "b.mp3", "p.m3u"), key = "Buch/CD1")),
      d(dir("CD2", *f("c.mp3", "d.mp3"), key = "Buch/CD2")),
      key = "Buch",
    )
    val plan = PlaylistPlanner.plan(book) { bytes("b.mp3", "a.mp3") }
    plan.mode shouldBe PlaylistMode.PER_DIR
    plan.rankByKey shouldBe mapOf("Buch/CD1/b.mp3" to 0, "Buch/CD1/a.mp3" to 1)
  }

  @Test
  fun noPlaylistMeansNonePlan() {
    PlaylistPlanner.plan(dir("Buch", *f("a.mp3"), key = "Buch")) { null } shouldBe PlaylistPlan.NONE
  }

  @Test
  fun unreadablePlaylistIsSkippedAsDefective() {
    val book = dir("Buch", *f("liste.m3u", "a.mp3"), key = "Buch")
    val plan = PlaylistPlanner.plan(book) { null }
    plan.mode shouldBe PlaylistMode.NONE
    plan.defective shouldBe true
  }

  @Test
  fun playlistSortKeyPutsListedFilesFirstInPlaylistOrderThenTheRestByName() {
    fun item(name: String, rank: Int?) = voice.data.ChapterSortItem(name, listOf("B"), name, null, 0L, rank)
    val items = listOf(item("z.mp3", 1), item("m.mp3", null), item("a.mp3", 0), item("b.mp3", null))
    val sorted = voice.data.ChapterSorter.sort(
      items,
      voice.data.ChapterSortSettings(voice.data.ChapterGrouping.FLAT, voice.data.ChapterSortKey.PLAYLIST),
    ).map { it.item }
    sorted shouldBe listOf("a.mp3", "z.mp3", "b.mp3", "m.mp3")
  }
}
