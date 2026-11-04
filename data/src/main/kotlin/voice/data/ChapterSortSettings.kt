package voice.data

/**
 * Wie die Dateien eines Buchs zu einer Reihenfolge zusammengesetzt werden.
 *
 * BY_FOLDER: Erst nach Ordnern (natuerliche Sortierung, Unterordner nach ihrem Elternordner),
 *            innerhalb eines Ordners nach [ChapterSortSettings.key].
 *            Ergebnis: Ordner 1 komplett (1..N), dann Ordner 2 komplett (1..N) usw.
 * FLAT:      Alle Dateien gemeinsam nach [ChapterSortSettings.key], Ordner spielen keine Rolle.
 */
enum class ChapterGrouping {
  BY_FOLDER,
  FLAT,
}

enum class ChapterSortKey {
  /** Dateiname ohne Endung, natuerlich sortiert (2 vor 10). */
  FILENAME,

  /** Titel aus den Tags (faellt auf den Dateinamen zurueck), natuerlich sortiert. */
  TITLE,

  /** Aenderungsdatum der Datei, aelteste zuerst. */
  DATE,

  /**
   * Reihenfolge einer m3u-Playlist. Der Rang jeder Datei kommt von aussen (siehe [ChapterSortItem.rank]),
   * Dateien ohne Rang folgen am Ende nach Dateiname.
   */
  PLAYLIST,

  /**
   * "Zaubern": erkennt aus den Zahlen in den Dateinamen des Ordners selbst, welche Zahl die CD und welche der
   * Track ist (siehe [MagicOrder]). Passt kein Muster, gilt der normale Namensvergleich.
   */
  MAGIC,
}

data class ChapterSortSettings(
  val grouping: ChapterGrouping = ChapterGrouping.BY_FOLDER,
  val key: ChapterSortKey = ChapterSortKey.FILENAME,
  val reversed: Boolean = false,
) {

  fun encode(): String = "${grouping.name}|${key.name}|${if (reversed) 1 else 0}"

  companion object {

    /** Liefert null bei ungueltigen Daten, dann gilt die Standard-Reihenfolge. */
    fun decode(value: String): ChapterSortSettings? {
      val parts = value.split("|")
      if (parts.size != 3) return null
      val grouping = ChapterGrouping.entries.firstOrNull { it.name == parts[0] } ?: return null
      val key = ChapterSortKey.entries.firstOrNull { it.name == parts[1] } ?: return null
      val reversed = when (parts[2]) {
        "1" -> true
        "0" -> false
        else -> return null
      }
      return ChapterSortSettings(grouping, key, reversed)
    }
  }
}
