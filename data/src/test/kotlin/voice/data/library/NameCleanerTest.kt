package voice.data.library

import io.kotest.matchers.shouldBe
import org.junit.Test

class NameCleanerTest {

  @Test
  fun removesReleaseTagsAndDots() {
    NameCleaner.clean("Karen.Test.-.2011.-.Todesstrafe.Germ.wOOt") shouldBe "Karen Test - 2011 - Todesstrafe"
    NameCleaner.clean("Max.Muster.-.Die.Reise.zum.Mond.MP3.Deutsch") shouldBe
      "Max Muster - Die Reise zum Mond"
    NameCleaner.clean("Erika.Beispiel.-.Sommerwind.Hoerbuch-wOOt") shouldBe "Erika Beispiel - Sommerwind"
  }

  @Test
  fun removesShortLeftoversOnlyBehindOtherTags() {
    NameCleaner.clean("Paul.Probe.-.Der.Nachtwanderer.M.wOOt") shouldBe "Paul Probe - Der Nachtwanderer"
    NameCleaner.clean("Lena.Muster.-.2010.-.Morgenrot.G.wOOt") shouldBe "Lena Muster - 2010 - Morgenrot"
    NameCleaner.clean("Agent M") shouldBe "Agent M"
  }

  @Test
  fun keepsDotsOfInitials() {
    NameCleaner.clean("A.B.C.Muster-Das.Buch.der.Zahlen") shouldBe "A. B. C. Muster-Das Buch der Zahlen"
    NameCleaner.clean("Sven.Test.-.P.S.Ich.warte.Hoerbuch-wOOt") shouldBe
      "Sven Test - P. S. Ich warte"
  }

  @Test
  fun removesTagGluedToTheLastWordAndTheExtension() {
    NameCleaner.clean("Sven Test - Sommerlied-Hörbuch.mp3") shouldBe "Sven Test - Sommerlied"
  }

  @Test
  fun removesBitrateOnlyWithItsUnit() {
    NameCleaner.clean("Winterlicht CD1 64bit") shouldBe "Winterlicht CD1"
    NameCleaner.clean("Anna.Beispiel.-.Nordlicht.Mp3.128.KBit") shouldBe "Anna Beispiel - Nordlicht"
    NameCleaner.clean("Mein Buch 07") shouldBe "Mein Buch 07"
    NameCleaner.clean("Muster - Die 12") shouldBe "Muster - Die 12"
  }

  @Test
  fun leavesCleanNamesAlone() {
    NameCleaner.clean("Max Muster - Das Mondlicht") shouldBe "Max Muster - Das Mondlicht"
    NameCleaner.clean("1191 - Der Fall Beispiel (Erika Muster, D), ABC-XYZ 2004") shouldBe
      "1191 - Der Fall Beispiel (Erika Muster, D), ABC-XYZ 2004"
    NameCleaner.clean("Muster") shouldBe "Muster"
  }

  @Test
  fun neverReturnsAnEmptyName() {
    NameCleaner.clean("Hoerbuch") shouldBe "Hoerbuch"
    NameCleaner.clean("mp3") shouldBe "mp3"
    NameCleaner.clean("   ") shouldBe "   "
  }

  @Test
  fun bookNameUsesTheParentForPartAndGenericFolderNames() {
    NameCleaner.bookName(listOf("Die.Buecher", "CD 4")) shouldBe "Die Buecher – CD 4"
    NameCleaner.bookName(listOf("Autor - Titel.Hoerbuch-wOOt", "mp3")) shouldBe "Autor - Titel"
    NameCleaner.bookName(listOf("Autor", "Der Titel")) shouldBe "Der Titel"
    NameCleaner.bookName(listOf("CD 4")) shouldBe "CD 4"
  }
}
