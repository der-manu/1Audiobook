package voice.data.library

import io.kotest.matchers.shouldBe
import org.junit.Test

class BookDetectorTest {

  private fun names(root: LibraryDir) = BookDetector.detect(root).books.map { it.pathNames.joinToString("/") }

  @Test
  fun everyFolderWithAudioIsABookAndAuthorFoldersAreContainers() {
    val root = dir(
      "Lib",
      d(dir("Autor A", d(dir("Buch 1", *numbered(5))), d(dir("Buch 2", *numbered(7))))),
      d(dir("Buch 3", *numbered(9))),
    )
    names(root) shouldBe listOf("Autor A/Buch 1", "Autor A/Buch 2", "Buch 3")
  }

  @Test
  fun cdFoldersMakeOneBookWithKindMultiPart() {
    val root = dir(
      "Lib",
      d(dir("Das Buch", d(dir("CD1", *numbered(10))), d(dir("CD 2", *numbered(10))), d(dir("cd3", *numbered(4))))),
    )
    val book = BookDetector.detect(root).books.single()
    book.kind shouldBe BookKind.MULTI_PART
    book.pathNames shouldBe listOf("Das Buch")
    book.fileCount shouldBe 24
  }

  @Test
  fun romanNumeralsAndDiscWordsCountAsParts() {
    val root = dir("Lib", d(dir("Buch", d(dir("CD I", *numbered(3))), d(dir("CD II", *numbered(3))))))
    BookDetector.detect(root).books.single().kind shouldBe BookKind.MULTI_PART
  }

  @Test
  fun siblingsThatDifferOnlyByNumbersAreParts() {
    val root = dir(
      "Lib",
      d(dir("Tess Gerritsen - Verrat in Paris", d(dir("Paris1", *numbered(3))), d(dir("Paris2", *numbered(3))))),
    )
    BookDetector.detect(root).books.single().kind shouldBe BookKind.MULTI_PART
  }

  @Test
  fun similarNamesWithoutAnyLinkToTheParentAreSeparateBooks() {
    val root = dir("Lib", d(dir("Autor A", d(dir("Buch 1", *numbered(3))), d(dir("Buch 2", *numbered(3))))))
    BookDetector.detect(root).books.size shouldBe 2
  }

  @Test
  fun manySimilarSiblingsAreEpisodesNotDiscs() {
    val episodes = (1..30).map { d(dir("Folge $it", *numbered(3))) }.toTypedArray()
    val root = dir("Lib", d(dir("Folge Hoerspielreihe", *episodes)))
    BookDetector.detect(root).books.size shouldBe 30
  }

  @Test
  fun aSingleDiscFolderInsideAWrapperBelongsToTheWrapper() {
    val root = dir("Lib", d(dir("Das Buch", d(dir("Cd 2", *numbered(8))))))
    val book = BookDetector.detect(root).books.single()
    book.pathNames shouldBe listOf("Das Buch")
    book.kind shouldBe BookKind.MULTI_PART
  }

  @Test
  fun nestedWrappersAroundDiscsStayOneBook() {
    val root = dir(
      "Lib",
      d(
        dir(
          "Max.Landorff.-.Der.Regler",
          d(dir("Landorff-1-Regler", d(dir("CD 1", *numbered(4))))),
          d(dir("Landorff-2-Regler", d(dir("CD 2", *numbered(4))))),
        ),
      ),
    )
    val book = BookDetector.detect(root).books.single()
    book.pathNames shouldBe listOf("Max.Landorff.-.Der.Regler")
    book.fileCount shouldBe 8
  }

  @Test
  fun chosenFolderThatIsItselfABookWithDiscsIsOneBook() {
    val root = dir("Das Buch", d(dir("CD1", *numbered(4))), d(dir("CD2", *numbered(4))))
    val book = BookDetector.detect(root).books.single()
    book.kind shouldBe BookKind.MULTI_PART
    book.fileCount shouldBe 8
  }

  @Test
  fun mixedFoldersAreFlaggedForReview() {
    val root = dir("Lib", d(dir("Sammlung", *f("a.mp3", "b.mp3"), d(dir("Unterordner", *numbered(3))))))
    val book = BookDetector.detect(root).books.single()
    book.kind shouldBe BookKind.MIXED
    (BookFlag.MIXED_STRUCTURE in book.flags) shouldBe true
    book.needsReview shouldBe true
  }

  @Test
  fun tinyAndHugeBooksAreFlaggedNormalOnesAreNot() {
    val root = dir(
      "Lib",
      d(dir("Winzig", *numbered(2))),
      d(dir("Normal", *numbered(20))),
      d(dir("Riesig", *numbered(320))),
    )
    val byName = BookDetector.detect(root).books.associateBy { it.pathNames.single() }
    byName.getValue("Winzig").flags shouldBe setOf(BookFlag.FEW_FILES)
    byName.getValue("Normal").flags shouldBe emptySet<BookFlag>()
    byName.getValue("Riesig").flags shouldBe setOf(BookFlag.MANY_FILES)
  }

  @Test
  fun looseAudioInTheRootIsNotTurnedIntoABook() {
    val root = dir("Lib", *f("lose.mp3"), d(dir("Buch", *numbered(5))))
    val result = BookDetector.detect(root)
    result.books.size shouldBe 1
    result.looseRootFiles shouldBe 1
  }

  @Test
  fun foldersWithoutAudioAreIgnored() {
    val root = dir("Lib", d(dir("Cover", *f("front.jpg"))), d(dir("Buch", *numbered(3))))
    names(root) shouldBe listOf("Buch")
  }

  @Test
  fun aFolderNamedLikeADiscInsideAContainerIsFlagged() {
    val root = dir("Lib", d(dir("Serie", d(dir("Teil A", *numbered(5))), d(dir("CD 2", *numbered(5))))))
    val book = BookDetector.detect(root).books.first { it.pathNames.last() == "CD 2" }
    (BookFlag.LOOKS_LIKE_PART in book.flags) shouldBe true
  }
}
