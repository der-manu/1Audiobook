package voice.data.library

/**
 * Macht aus Ordnernamen wie "Karen.Rose.-.2011.-.Todesstoss.Germ.wOOt" lesbare Buchtitel.
 * Die Regeln sind bewusst vorsichtig: Es werden nur eindeutige Release-Anhaengsel am Ende entfernt,
 * und der Nutzer kann jeden Vorschlag in der Vorschau aendern.
 */
object NameCleaner {

  /** Eindeutige Anhaengsel, vergleichen in Kleinbuchstaben. */
  private val STRONG_JUNK = setOf(
    "woot", "mp3", "hoerbuch", "hörbuch", "audiobook", "abook", "deutsch", "german", "germ", "kbit", "kbps",
    "lame", "cbr", "vbr",
  )

  /** Kurze Reste, die nur direkt hinter anderen Anhaengseln als Anhaengsel gelten (z. B. "...Lustmolch.M.wOOt"). */
  private val WEAK_JUNK = setOf("m", "mp", "g", "ger", "bit", "ogg", "flac")

  /** Ordnernamen, die nichts ueber das Buch sagen. Dann zaehlt der Elternordner. */
  private val GENERIC_NAMES = setOf(
    "mp3", "mp3s", "flac", "ogg", "m4a", "m4b", "audio", "audios", "hoerbuch", "hörbuch", "sound", "musik",
    "hoerspiel", "hörspiel", "kapitel", "cd", "disc", "disk",
  )

  private val BITRATE_WITH_UNIT = Regex("""^\d{2,3}(k?bit|kbps)$""", RegexOption.IGNORE_CASE)
  private val PLAIN_BITRATE = Regex("""^\d{2,3}$""")
  private val BITRATE_UNITS = setOf("kbit", "kbps", "bit")
  private val TRAILING_EXTENSION = Regex("""\.(mp3|m4a|m4b|ogg|flac|wav)$""", RegexOption.IGNORE_CASE)
  private val INITIAL_WITH_DOT = Regex("""(?<![\p{L}\d])(\p{Lu})\.""")
  private val SPACES = Regex("""\s+""")
  private val EDGE_SEPARATORS = Regex("""^[\s\-_.,;:]+|[\s\-_.,;:]+$""")
  private const val INITIAL_MARK = '\u0001'

  /** Titel fuer einen Ordnernamen. Liefert nie einen leeren Text. */
  fun clean(raw: String): String {
    var s = raw.trim().replace(TRAILING_EXTENSION, "")
    if (s.isEmpty()) return raw
    s = s.replace('_', ' ')
    // Punkte als Wortgrenze nur, wenn der Name offensichtlich mit Punkten statt Leerzeichen geschrieben ist.
    val dots = s.count { it == '.' }
    val spaces = s.count { it == ' ' }
    if ((dots >= 1 && spaces == 0) || (dots >= 2 && dots > spaces)) {
      // Initialen ("J.R.R.Tolkien") behalten ihren Punkt: "J. R. R. Tolkien".
      s = s.replace(INITIAL_WITH_DOT) { "${it.groupValues[1]}$INITIAL_MARK" }
      s = s.replace('.', ' ').replace("$INITIAL_MARK", ". ")
    }
    s = s.replace(SPACES, " ")

    val tokens = s.split(' ').filter { it.isNotEmpty() }.toMutableList()
    var removedJunk = false
    while (tokens.isNotEmpty()) {
      var token = tokens.last()
      var dropped = false
      // Anhaengsel von hinten abtrennen, auch wenn sie ueber "-" oder "." am letzten Wort haengen.
      while (token.isNotEmpty()) {
        val separator = maxOf(token.lastIndexOf('-'), token.lastIndexOf('.'))
        val candidate = token.substring(separator + 1).trim('(', ')', '[', ']', ',').lowercase()
        val isJunk = candidate.isEmpty() || candidate in STRONG_JUNK ||
          (removedJunk && candidate in WEAK_JUNK) || BITRATE_WITH_UNIT.matches(candidate)
        if (!isJunk) break
        if (candidate.isNotEmpty()) removedJunk = true
        dropped = true
        token = if (separator >= 0) token.substring(0, separator) else ""
        // "MP3 128 KBit": die Zahl vor der Einheit ist die Bitrate und gehoert dazu.
        if (candidate in BITRATE_UNITS && token.isEmpty() && tokens.size >= 2 &&
          PLAIN_BITRATE.matches(tokens[tokens.lastIndex - 1])
        ) {
          tokens.removeAt(tokens.lastIndex - 1)
        }
      }
      if (!dropped) break
      if (token.isEmpty()) {
        tokens.removeAt(tokens.lastIndex)
      } else {
        tokens[tokens.lastIndex] = token
        break
      }
    }
    val result = tokens.joinToString(" ").replace(EDGE_SEPARATORS, "").replace(SPACES, " ").trim()
    return if (result.length >= 2) result else raw.trim()
  }

  /**
   * Buchtitel aus dem Pfad. Heisst der Buchordner nur "CD 4", "11" oder "mp3", wird der Elternordner genutzt.
   */
  fun bookName(pathNames: List<String>): String {
    val last = pathNames.lastOrNull() ?: return ""
    val cleanedLast = clean(last)
    val parent = pathNames.getOrNull(pathNames.size - 2)
    return when {
      parent == null -> cleanedLast
      cleanedLast.lowercase() in GENERIC_NAMES -> clean(parent)
      BookDetector.isPartName(last) -> "${clean(parent)} – $cleanedLast"
      else -> cleanedLast
    }
  }
}
