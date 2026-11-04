package voice.data

/**
 * "Zaubern": erkennt aus den Dateinamen eines Ordners, wenn mehrere CDs in einem Ordner vermischt sind, und stellt
 * CD 1 komplett vor CD 2.
 *
 * Beispiel: "01 - Titel (CD1).mp3", "01 - Titel (CD2).mp3", "02 - Titel (CD1).mp3" ...
 * Ein Namensvergleich ergibt 01/CD1, 01/CD2, 02/CD1 ..., weil die Track-Nummer vorn steht.
 *
 * Der Grundsatz: Eine Umsortierung lohnt sich nur, wenn die fuehrende Nummer NICHT eindeutig ist, also dieselbe
 * Track-Nummer auf mehreren CDs vorkommt. Ist sie eindeutig (durchlaufende Zaehlung), loest der normale
 * Namensvergleich das Problem schon, und hier greift nichts ein.
 *
 * Die CD-Zahl kommt entweder hinter einem CD-Wort ("CD", "Disc", "Teil", "Part" ...) oder ist die nachgestellte Zahl
 * des Namens. In beiden Faellen muss ein regelmaessiges Raster entstehen: wenige CDs, viele Tracks, und die meisten
 * Kombinationen kommen vor. Sonst gibt es keinen Schluessel.
 */
object MagicOrder {

  private val NUMBER = Regex("""\d+""")
  private val LEADING = Regex("""^[^\p{L}\d]*(\d+)""")
  private val DISC = Regex(
    """(?i)(?<!\p{L})(?:cd|disc|disk|dvd|teil|part|band|vol|volume|side|seite|kassette|mc)[\s._\-]*\(?(\d{1,3})""",
  )

  /** Mindestanteil der Dateien, die an dem Raster beteiligt sein muessen. */
  private const val MIN_SHARE = 0.6

  /** Mindestanteil der Raster-Felder (CDs x Tracks), die tatsaechlich belegt sind. */
  private const val MIN_COMPLETENESS = 0.5

  /** Eine "CD-Zahl" hat nur wenige verschiedene Werte. */
  private const val MAX_DISCS = 12

  private const val MAX_DIGITS = 15

  /** Fehlende Zahl: kommt vor allen vorhandenen, z. B. "Intro.mp3" in einem Ordner mit CD-Angaben. */
  private const val MISSING = -1L

  private data class Fields(
    val disc: Long?,
    val leading: Long?,
    val trailing: Long?,
  )

  /**
   * @return je Dateiname einen Schluessel (CD, Track; lexikographisch zu vergleichen). Alle Schluessel sind null,
   *   wenn kein klares Muster erkannt wird.
   */
  fun keys(fileNames: List<String>): List<List<Long>?> {
    val none = List<List<Long>?>(fileNames.size) { null }
    if (fileNames.size < 2) return none
    val fields = fileNames.map(::parse)
    val coarse = listOf<(Fields) -> Long?>({ it.disc }, { it.trailing })
      .firstOrNull { selector -> formsGrid(fields, selector) }
      ?: return none
    return fields.map { listOf(coarse(it) ?: MISSING, it.leading ?: MISSING) }
  }

  /** Bilden CD-Zahl (coarse) und fuehrende Nummer zusammen ein regelmaessiges Raster, und lohnt sich Umsortieren? */
  private fun formsGrid(
    fields: List<Fields>,
    coarse: (Fields) -> Long?,
  ): Boolean {
    val pairs = fields.mapNotNull { f ->
      val c = coarse(f)
      val t = f.leading
      if (c != null && t != null) c to t else null
    }
    if (pairs.size < MIN_SHARE * fields.size) return false
    val discs = pairs.map { it.first }.toSet()
    val tracks = pairs.map { it.second }.toSet()
    if (discs.size !in 2..MAX_DISCS) return false
    // Die CD-Zahl hat die kleinere Dimension; sind beide gleich gross, ist nicht klar, welche die CD ist.
    if (discs.size >= tracks.size) return false
    // Eindeutige Track-Nummern brauchen keine CD-Gruppierung: der Namensvergleich ordnet sie schon richtig.
    if (tracks.size == pairs.size) return false
    val filled = pairs.toSet().size.toDouble() / (discs.size * tracks.size)
    return filled >= MIN_COMPLETENESS
  }

  private fun parse(fileName: String): Fields {
    val base = fileName.substringBeforeLast('.', fileName)
    val leadingMatch = LEADING.find(base)?.groups?.get(1)
    val leading = leadingMatch?.value?.toNumber()
    val last = NUMBER.findAll(base).lastOrNull()
    // Gibt es nur eine Zahl, ist sie die fuehrende und keine nachgestellte.
    val trailing = last?.takeIf { leadingMatch == null || it.range.first != leadingMatch.range.first }
      ?.value?.toNumber()
    val disc = DISC.find(base)?.groupValues?.get(1)?.toNumber()
    return Fields(disc = disc, leading = leading, trailing = trailing)
  }

  private fun String.toNumber(): Long = take(MAX_DIGITS).toLong()
}
