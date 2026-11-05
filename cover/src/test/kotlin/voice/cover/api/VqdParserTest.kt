package voice.cover.api

import io.kotest.matchers.shouldBe
import org.junit.Test

internal class VqdParserTest {

  @Test
  fun readsTheTokenInAllKnownPageFormats() {
    parseVqd("<script>...&vqd=4-123456789012&p=1...") shouldBe "4-123456789012"
    parseVqd("""var x = {vqd="4-123456789012", other: 1};""") shouldBe "4-123456789012"
    parseVqd("""vqd='4-123456789012';""") shouldBe "4-123456789012"
    parseVqd("""{"foo":1,"vqd":"4-123456789012","bar":2}""") shouldBe "4-123456789012"
    parseVqd("vqd=3-12345-67890&x") shouldBe "3-12345-67890"
  }

  @Test
  fun returnsNullWithoutAToken() {
    parseVqd("") shouldBe null
    parseVqd("<html>no token here</html>") shouldBe null
    parseVqd("vqd=abc&") shouldBe null
  }
}
