package voice.cover

import io.kotest.matchers.shouldBe
import org.junit.Test

internal class QuerySuggestionsTest {

  @Test
  fun stripsNoiseAndSplitsAuthorFromTitle() {
    QuerySuggestions.build("Karen Test - Der Sommer (ungekürzt)", null) shouldBe listOf(
      "Der Sommer",
      "Karen Test Der Sommer",
      "Der Sommer Hörbuch",
      "Der Sommer audiobook",
    )
  }

  @Test
  fun usesTheAuthorWhenKnown() {
    QuerySuggestions.build("Der Sommer", "Karen Test") shouldBe listOf(
      "Der Sommer",
      "Karen Test Der Sommer",
      "Der Sommer Hörbuch",
      "Der Sommer audiobook",
    )
  }

  @Test
  fun removesTheAuthorPrefixOfTheTitle() {
    QuerySuggestions.build("Karen Test - Der Sommer", "Karen Test").first() shouldBe "Der Sommer"
    QuerySuggestions.build("Karen Test Der Sommer", "Karen Test").first() shouldBe "Der Sommer"
  }

  @Test
  fun doesNotRepeatTheAuthorWhenTheTitleContainsIt() {
    QuerySuggestions.build("Karen Tests Reise", "Karen Test").contains("Karen Test Karen Tests Reise") shouldBe false
  }

  @Test
  fun dropsBracketsAndFormatWords() {
    QuerySuggestions.build("Mein Buch [Hörbuch] CD 2", null).first() shouldBe "Mein Buch"
  }

  @Test
  fun returnsNothingForEmptyNames() {
    QuerySuggestions.build("", null) shouldBe emptyList<String>()
    QuerySuggestions.build("   ", "Karen Test") shouldBe emptyList<String>()
    QuerySuggestions.build("(ungekürzt)", null) shouldBe emptyList<String>()
  }

  @Test
  fun respectsTheLimitAndHasNoDuplicates() {
    val result = QuerySuggestions.build("Der Sommer", "Karen Test", limit = 2)
    result.size shouldBe 2
    QuerySuggestions.build("Der Sommer", "").map { it.lowercase() }.toSet().size shouldBe
      QuerySuggestions.build("Der Sommer", "").size
  }
}
