package voice.data

import io.kotest.matchers.shouldBe
import org.junit.Test

class MagicOrderTest {

  private val magic = ChapterSortSettings(ChapterGrouping.BY_FOLDER, ChapterSortKey.MAGIC, false)

  private fun order(
    names: List<String>,
    folder: List<String> = listOf("Buch"),
  ): List<String> {
    val items = names.map { ChapterSortItem(it, folder, it, null, 0L) }
    return ChapterSorter.sort(items, magic).map { it.item }
  }

  private fun plain(names: List<String>): List<String> {
    val items = names.map { ChapterSortItem(it, listOf("Buch"), it, null, 0L) }
    return ChapterSorter.sort(items, ChapterSortSettings()).map { it.item }
  }

  private fun wickert(vararg tracksPerDisc: Int): List<String> =
    tracksPerDisc.flatMapIndexed { disc, tracks ->
      (1..tracks).map { "%02d - Der Richter aus Paris (CD%d).mp3".format(it, disc + 1) }
    }

  @Test
  fun putsEachCdCompletelyBeforeTheNextWhenTrackNumbersRepeat() {
    val names = wickert(20, 20, 16)
    val expected = wickert(20, 20, 16)
    order(names.shuffled(java.util.Random(5))) shouldBe expected
  }

  @Test
  fun aPlainNameComparisonGetsThisCaseWrongWhichIsTheReasonForMagic() {
    val names = wickert(5, 5, 5)
    plain(names).take(3) shouldBe listOf(
      "01 - Der Richter aus Paris (CD1).mp3",
      "01 - Der Richter aus Paris (CD2).mp3",
      "01 - Der Richter aus Paris (CD3).mp3",
    )
    order(names).take(3) shouldBe listOf(
      "01 - Der Richter aus Paris (CD1).mp3",
      "02 - Der Richter aus Paris (CD1).mp3",
      "03 - Der Richter aus Paris (CD1).mp3",
    )
  }

  @Test
  fun worksWithoutACdWordWhenASmallTrailingNumberIsTheCd() {
    val names = listOf(1, 2).flatMap { disc -> (1..8).map { "%02d - Titel (%d).mp3".format(it, disc) } }
    order(names.shuffled(java.util.Random(1))) shouldBe names
  }

  @Test
  fun leavesAContinuousCountAlone() {
    val names = (1..40).map { "%02d - Titel (CD%d).mp3".format(it, if (it <= 20) 1 else 2) }
    MagicOrder.keys(names).all { it == null } shouldBe true
    order(names.shuffled(java.util.Random(3))) shouldBe plain(names)
  }

  @Test
  fun leavesDiscPrefixedNamesAlone() {
    val names = (1..3).flatMap { disc -> (1..9).map { "$disc-%03d.mp3".format(it) } }
    MagicOrder.keys(names).all { it == null } shouldBe true
    order(names.shuffled(java.util.Random(2))) shouldBe plain(names)
  }

  @Test
  fun leavesPartWordsInsideStoriesAlone() {
    val names = listOf(
      "01-Der Elsterverein -Teil 01.mp3", "02-Der Elsterverein -Teil 02.mp3", "03-Der Elsterverein -Teil 03.mp3",
      "04-Der direkte Draht-Teil 01.mp3", "05-Der direkte Draht-Teil 02.mp3", "06-Tod eines Architekten -Teil 1.mp3",
    )
    MagicOrder.keys(names).all { it == null } shouldBe true
  }

  @Test
  fun refusesAmbiguousSquareGrids() {
    val names = (1..3).flatMap { disc -> (1..3).map { "%02d - Titel (CD%d).mp3".format(it, disc) } }
    MagicOrder.keys(names).all { it == null } shouldBe true
  }

  @Test
  fun refusesNamesWithoutNumbersAndTooFewFiles() {
    MagicOrder.keys(listOf("Anfang.mp3", "Mitte.mp3", "Ende.mp3")).all { it == null } shouldBe true
    MagicOrder.keys(listOf("01 - A (CD1).mp3")).all { it == null } shouldBe true
    MagicOrder.keys(emptyList()) shouldBe emptyList<List<Long>?>()
  }

  @Test
  fun filesWithoutNumbersComeFirstInsteadOfLast() {
    val names = wickert(6, 6) + "Intro.mp3"
    order(names).first() shouldBe "Intro.mp3"
  }

  @Test
  fun eachFolderIsAnalysedOnItsOwnWhenGroupedByFolder() {
    val mixed = wickert(5, 5, 5)
    val plainFolder = listOf("10 - Anfang.mp3", "02 - Mitte.mp3", "03 - Ende.mp3")
    val items = mixed.map { ChapterSortItem(it, listOf("A"), it, null, 0L) } +
      plainFolder.map { ChapterSortItem(it, listOf("B"), it, null, 0L) }
    val sorted = ChapterSorter.sort(items, magic).map { it.item }
    sorted.take(5) shouldBe wickert(5, 5, 5).take(5)
    sorted.takeLast(3) shouldBe listOf("02 - Mitte.mp3", "03 - Ende.mp3", "10 - Anfang.mp3")
  }

  @Test
  fun magicSettingsSurviveEncoding() {
    val settings = ChapterSortSettings(ChapterGrouping.FLAT, ChapterSortKey.MAGIC, true)
    ChapterSortSettings.decode(settings.encode()) shouldBe settings
  }
}
