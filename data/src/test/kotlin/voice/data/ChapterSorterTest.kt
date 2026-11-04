package voice.data

import io.kotest.matchers.shouldBe
import org.junit.Test

class ChapterSorterTest {

  private fun item(
    path: String,
    title: String? = null,
    date: Long = 0L,
  ): ChapterSortItem<String> {
    val segments = "tree/primary:Books/document/primary:Books/$path".split("/")
    return ChapterSortItem(path, segments.dropLast(1), segments.last(), title, date)
  }

  private fun sort(
    items: List<ChapterSortItem<String>>,
    settings: ChapterSortSettings,
  ) = ChapterSorter.sort(items, settings).map { it.item }

  @Test
  fun byFolderKeepsEachFolderTogetherAndSortsFoldersNaturally() {
    val items = listOf(
      "Book/CD10/01.mp3", "Book/CD2/Extra/01.mp3", "Book/CD1/10 End.mp3", "Book/Intro.mp3",
      "Book/CD2/02 B.mp3", "Book/CD1/02 B.mp3", "Book/CD2/01 A.mp3", "Book/CD1/01 A.mp3",
    ).map { item(it) }

    sort(items, ChapterSortSettings()) shouldBe listOf(
      "Book/Intro.mp3",
      "Book/CD1/01 A.mp3", "Book/CD1/02 B.mp3", "Book/CD1/10 End.mp3",
      "Book/CD2/01 A.mp3", "Book/CD2/02 B.mp3", "Book/CD2/Extra/01.mp3",
      "Book/CD10/01.mp3",
    )
  }

  @Test
  fun flatSortingMixesFoldersWhilstByFolderDoesNot() {
    val items = (1..5).flatMap { folder -> (1..3).map { track -> item("Show/Part $folder/track$track.mp3") } }
      .shuffled(java.util.Random(7))

    sort(items, ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.FILENAME))
      .take(5).map { it.substringAfterLast('/') } shouldBe List(5) { "track1.mp3" }

    sort(items, ChapterSortSettings(ChapterGrouping.BY_FOLDER, ChapterSortKey.FILENAME))
      .take(6) shouldBe listOf(
      "Show/Part 1/track1.mp3", "Show/Part 1/track2.mp3", "Show/Part 1/track3.mp3",
      "Show/Part 2/track1.mp3", "Show/Part 2/track2.mp3", "Show/Part 2/track3.mp3",
    )
  }

  @Test
  fun flatFileNameSortingIsAlphabeticalAndNatural() {
    val items = listOf("X/zebra.mp3", "Y/Apple.mp3", "X/pear.mp3", "Y/chapter 10.mp3", "X/Chapter 2.mp3").map { item(it) }

    sort(items, ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.FILENAME)) shouldBe listOf(
      "Y/Apple.mp3", "X/Chapter 2.mp3", "Y/chapter 10.mp3", "X/pear.mp3", "X/zebra.mp3",
    )
  }

  @Test
  fun titleSortingFallsBackToFileName() {
    val items = listOf(
      item("B/a.mp3", title = "Chapter 10"),
      item("B/b.mp3", title = "Chapter 2"),
      item("B/c.mp3", title = null),
    )

    sort(items, ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.TITLE)) shouldBe
      listOf("B/c.mp3", "B/b.mp3", "B/a.mp3")
  }

  @Test
  fun dateSortingAndReverse() {
    val items = listOf(item("B/a.mp3", date = 300), item("B/b.mp3", date = 100), item("B/c.mp3", date = 200))

    sort(items, ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.DATE)) shouldBe
      listOf("B/b.mp3", "B/c.mp3", "B/a.mp3")
    sort(items, ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.DATE, reversed = true)) shouldBe
      listOf("B/a.mp3", "B/c.mp3", "B/b.mp3")
  }

  @Test
  fun resultDoesNotDependOnInputOrder() {
    val items = listOf(item("B/cd1/01.mp3"), item("B/CD1/01.mp3"), item("B/CD2/01.mp3"))

    sort(items, ChapterSortSettings()) shouldBe sort(items.reversed(), ChapterSortSettings())
  }

  @Test
  fun previewShowsFoldersRelativeToTheCommonParent() {
    val items = listOf(
      "Book/CD2/Bonus/01.mp3", "Book/Intro.mp3", "Book/CD1/01.mp3",
    ).map { item(it) }

    ChapterSorter.toPreview(ChapterSorter.sort(items, ChapterSortSettings()))
      .map { it.relativeFolder } shouldBe listOf("", "CD1", "CD2/Bonus")
    ChapterSorter.toPreview(listOf(item("B/a.mp3"), item("B/b.mp3")))
      .map { it.relativeFolder } shouldBe listOf("", "")
  }

  @Test
  fun settingsSurviveEncodingAndInvalidDataIsRejected() {
    for (grouping in ChapterGrouping.entries) {
      for (key in ChapterSortKey.entries) {
        for (reversed in listOf(true, false)) {
          val settings = ChapterSortSettings(grouping, key, reversed)
          ChapterSortSettings.decode(settings.encode()) shouldBe settings
        }
      }
    }
    listOf("", "x", "FLAT|FILENAME", "FLAT|NOPE|0", "NOPE|FILENAME|0", "FLAT|FILENAME|2")
      .map { ChapterSortSettings.decode(it) } shouldBe List(6) { null }
  }
}
