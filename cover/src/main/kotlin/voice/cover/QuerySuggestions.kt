package voice.cover

/**
 * Schlaegt kuerzere Suchbegriffe vor. Ein langer Titel samt Zusaetzen wie "(ungekuerzt)" findet bei der
 * Bildersuche oft nichts, der reine Titel dagegen schon.
 */
internal object QuerySuggestions {

  private val BRACKETS = Regex("""\s*[(\[][^)\]]*[)\]]""")
  private val NOISE = Regex(
    """(?i)(?<![\p{L}\d])(ungekürzt|ungekuerzt|gekürzt|gekuerzt|hörbuch|hoerbuch|hörspiel|hoerspiel|audiobook|mp3|cd\s*\d+)(?![\p{L}\d])""",
  )
  private val SPACES = Regex("""\s+""")
  private const val SEPARATOR = " - "

  fun build(
    bookName: String,
    author: String?,
    limit: Int = 4,
  ): List<String> {
    var title = tidy(bookName)
    var writer = author?.trim().orEmpty()

    // "Autor - Titel": den Autor vom Titel trennen.
    val parts = title.split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.size >= 2 && (writer.isEmpty() || parts.first().equals(writer, ignoreCase = true))) {
      if (writer.isEmpty()) writer = parts.first()
      title = parts.drop(1).joinToString(SEPARATOR)
    } else if (writer.isNotEmpty() && title.startsWith(writer, ignoreCase = true) && title.length > writer.length) {
      title = tidy(title.substring(writer.length))
    }
    if (title.isEmpty()) return emptyList()

    val withWriter = if (writer.isNotEmpty() && !title.contains(writer, ignoreCase = true)) "$writer $title" else null
    return listOfNotNull(title, withWriter, "$title Hörbuch", "$title audiobook")
      .distinctBy { it.lowercase() }
      .take(limit)
  }

  private fun tidy(raw: String): String {
    return raw.replace(BRACKETS, "")
      .replace(NOISE, " ")
      .replace(SPACES, " ")
      .trim(' ', '-', '.', ',', ':', ';', '_')
  }
}
