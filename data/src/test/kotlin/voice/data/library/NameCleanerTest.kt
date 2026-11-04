package voice.data.library

import io.kotest.matchers.shouldBe
import org.junit.Test

class NameCleanerTest {

  @Test
  fun removesReleaseTagsAndDots() {
    NameCleaner.clean("Karen.Rose.-.2011.-.Todesstoss.Germ.wOOt") shouldBe "Karen Rose - 2011 - Todesstoss"
    NameCleaner.clean("Douglas.Adams.-.Per.Anhalter.durch.die.Galaxis.MP3.Deutsch") shouldBe
      "Douglas Adams - Per Anhalter durch die Galaxis"
    NameCleaner.clean("Bernd.Michels.-.Mafiakind.Hoerbuch-wOOt") shouldBe "Bernd Michels - Mafiakind"
  }

  @Test
  fun removesShortLeftoversOnlyBehindOtherTags() {
    NameCleaner.clean("Christopher.Moore.-.Der.Lustmolch.M.wOOt") shouldBe "Christopher Moore - Der Lustmolch"
    NameCleaner.clean("Milena.Moser.-.2010.-.Moechtegern.G.wOOt") shouldBe "Milena Moser - 2010 - Moechtegern"
    NameCleaner.clean("Agent M") shouldBe "Agent M"
  }

  @Test
  fun keepsDotsOfInitials() {
    NameCleaner.clean("J.R.R.Tolkien-Der.Herr.der.Ringe") shouldBe "J. R. R. Tolkien-Der Herr der Ringe"
    NameCleaner.clean("Sebastian.Fitzek.-.P.S.Ich.toete.dich.Hoerbuch-wOOt") shouldBe
      "Sebastian Fitzek - P. S. Ich toete dich"
  }

  @Test
  fun removesTagGluedToTheLastWordAndTheExtension() {
    NameCleaner.clean("Sebastian Fitzek - Nachtwandler-Hörbuch.mp3") shouldBe "Sebastian Fitzek - Nachtwandler"
  }

  @Test
  fun removesBitrateOnlyWithItsUnit() {
    NameCleaner.clean("Eisfieber CD1 64bit") shouldBe "Eisfieber CD1"
    NameCleaner.clean("Arnaldur.Indridason.-.Kaeltezone.Mp3.128.KBit") shouldBe "Arnaldur Indridason - Kaeltezone"
    NameCleaner.clean("Harry Potter 07") shouldBe "Harry Potter 07"
    NameCleaner.clean("Fitzek - Die 12") shouldBe "Fitzek - Die 12"
  }

  @Test
  fun leavesCleanNamesAlone() {
    NameCleaner.clean("Ken Follett - Mitternachtsfalken") shouldBe "Ken Follett - Mitternachtsfalken"
    NameCleaner.clean("1191 - Die Affaere Leopold-Loeb (Rolf Schneider, D), RBB-MDR 2004") shouldBe
      "1191 - Die Affaere Leopold-Loeb (Rolf Schneider, D), RBB-MDR 2004"
    NameCleaner.clean("Nuhr") shouldBe "Nuhr"
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
