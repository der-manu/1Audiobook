package voice.data

import android.net.Uri
import io.kotest.matchers.shouldBe
import org.junit.Test
import voice.documentfile.CachedDocumentFile

class JunkFilesTest {

  private class Fake(
    override val name: String?,
    override val isDirectory: Boolean = false,
    override val children: List<CachedDocumentFile> = emptyList(),
  ) : CachedDocumentFile {
    override val isFile: Boolean get() = !isDirectory
    override val length: Long get() = 0L
    override val lastModified: Long get() = 0L
    override val uri: Uri get() = error("not needed in this test")
  }

  private fun file(name: String) = Fake(name)

  private fun dir(
    name: String,
    vararg children: CachedDocumentFile,
  ) = Fake(name, isDirectory = true, children = children.toList())

  @Test
  fun recognisesMacOsLeftoversAndHiddenNames() {
    isJunkName("._028 - Kapitel 028.mp3") shouldBe true
    isJunkName(".DS_Store") shouldBe true
    isJunkName(".hidden") shouldBe true
    isJunkName("__MACOSX") shouldBe true
    isJunkName("028 - Kapitel 028.mp3") shouldBe false
    isJunkName("Mein.Buch.mp3") shouldBe false
    isJunkName("a._b.mp3") shouldBe false
    isJunkName(null) shouldBe false
  }

  @Test
  fun resourceForkFilesAreNotAudioFiles() {
    file("028 - Kapitel 028.mp3").isAudioFile() shouldBe true
    file("._028 - Kapitel 028.mp3").isAudioFile() shouldBe false
    file(".hidden.ogg").isAudioFile() shouldBe false
  }

  @Test
  fun audioFileCountIgnoresJunkFilesAndHiddenFolders() {
    val book = dir(
      "Buch",
      file("01.mp3"),
      file("._01.mp3"),
      file("02.mp3"),
      file("._02.mp3"),
      dir("CD2", file("01.mp3"), file("._01.mp3")),
      dir("__MACOSX", file("._01.mp3"), dir("CD2", file("._01.mp3"))),
      dir(".Trash", file("geloescht.mp3")),
    )
    book.audioFileCount() shouldBe 3
  }

  @Test
  fun aSingleAudioFileCountsOneAndAJunkFileZero() {
    file("a.mp3").audioFileCount() shouldBe 1
    file("._a.mp3").audioFileCount() shouldBe 0
    file("cover.jpg").audioFileCount() shouldBe 0
  }
}
