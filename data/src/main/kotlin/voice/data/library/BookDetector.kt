package voice.data.library

enum class BookKind {
  /** Ein Ordner mit Audiodateien (ohne Unterordner mit Audio). */
  SINGLE,

  /** Ein Ordner, dessen Unterordner CDs/Teile desselben Buchs sind (CD1, CD2, ...). */
  MULTI_PART,

  /** Audiodateien liegen direkt im Ordner und zusaetzlich in Unterordnern. */
  MIXED,
}

enum class BookFlag {
  /** Nur 1 bis 2 Dateien: vielleicht ein Einzeltitel, vielleicht ein Fehlfund. */
  FEW_FILES,

  /** Sehr viele Dateien: oft mehrere Titel in einem Ordner. */
  MANY_FILES,

  /** Dateien direkt im Ordner und in Unterordnern: Struktur ist uneindeutig. */
  MIXED_STRUCTURE,

  /** Der Ordnername sieht aus wie eine CD (z. B. "CD 2"), steht aber nicht in einer erkannten CD-Reihe. */
  LOOKS_LIKE_PART,
}

data class DetectedBook(
  val dir: LibraryDir,
  /** Ordnernamen von der Bibliothekswurzel (ohne Wurzel) bis zum Buchordner. */
  val pathNames: List<String>,
  val kind: BookKind,
  val flags: Set<BookFlag>,
) {
  val fileCount: Int get() = dir.audioFileCount

  /** Soll der Nutzer sich das ansehen? */
  val needsReview: Boolean get() = flags.isNotEmpty()
}

data class DetectionResult(
  val books: List<DetectedBook>,
  /** Audiodateien direkt in der Wurzel neben anderen Buechern. Sie werden nicht automatisch aufgenommen. */
  val looseRootFiles: Int,
)

object BookDetector {

  const val FEW_FILES_MAX = 2
  const val MANY_FILES_MIN = 300

  /** Mehr gleichnamige Geschwisterordner als das sind Folgen einer Reihe, keine CDs eines Buchs. */
  const val SIMILAR_SIBLINGS_MAX = 24

  private val DISC_NAME = Regex(
    """(?i)(?<!\p{L})(cd|disc|disk|dvd|teil|part|tl|vol|volume|band|side|seite|kassette|mc)[\s._\-]*\(?(\d+|[ivx]{1,4}(?![\p{L}\d]))""" +
      """|^\(\d+\)""" + """|^\d{1,3}$""",
  )

  private val DIGITS = Regex("""\d+""")
  private val NON_LETTERS = Regex("""[^\p{L}]+""")

  /** Sieht der Ordnername wie eine CD / ein Teil aus? */
  fun isPartName(name: String): Boolean = DISC_NAME.containsMatchIn(name.trim())

  private fun shape(name: String): String = name.lowercase().replace(DIGITS, "#")

  fun detect(root: LibraryDir): DetectionResult {
    val books = ArrayList<DetectedBook>()
    val kids = root.subDirs.filter { it.hasAudio }
    return if (kids.isEmpty()) {
      if (root.audioFiles.isNotEmpty()) books += makeBook(root, emptyList(), BookKind.SINGLE)
      DetectionResult(books, looseRootFiles = 0)
    } else if (root.audioFiles.isEmpty() && (isPartSet(kids, root.name) || (kids.size == 1 && isPartName(kids[0].name)))) {
      // Der gewaehlte Ordner ist selbst ein Buch, dessen Unterordner die CDs sind.
      books += makeBook(root, emptyList(), BookKind.MULTI_PART)
      DetectionResult(books, looseRootFiles = 0)
    } else {
      walk(root, emptyList(), books, isRoot = true)
      DetectionResult(books, looseRootFiles = root.audioFiles.size)
    }
  }

  private fun walk(
    dir: LibraryDir,
    path: List<String>,
    out: MutableList<DetectedBook>,
    isRoot: Boolean = false,
  ) {
    val kids = dir.subDirs.filter { it.hasAudio }
    val own = dir.audioFiles
    when {
      kids.isEmpty() -> if (own.isNotEmpty()) out += makeBook(dir, path, BookKind.SINGLE)
      isRoot -> kids.forEach { walk(it, path + it.name, out) }
      own.isNotEmpty() -> out += makeBook(dir, path, BookKind.MIXED)
      isPartSet(kids, dir.name) -> out += makeBook(dir, path, BookKind.MULTI_PART)
      kids.size == 1 && isPartName(kids[0].name) -> out += makeBook(dir, path, BookKind.MULTI_PART)
      else -> kids.forEach { walk(it, path + it.name, out) }
    }
  }

  /**
   * Sind die Unterordner CDs/Teile eines Buchs? Entweder heissen alle wie eine CD ("CD1", "Disc 2", "Teil 3"),
   * oder sie unterscheiden sich nur durch Zahlen UND der gemeinsame Namensteil kommt auch im Namen des
   * Elternordners vor ("Reise nach Rom" / "Rom1", "Rom2"). Letzteres trennt CDs von Folgen einer Reihe.
   */
  private fun isPartSet(kids: List<LibraryDir>, parentName: String): Boolean {
    if (kids.size < 2) return false
    if (kids.all { isPartName(it.name) }) return true
    if (kids.size > SIMILAR_SIBLINGS_MAX) return false
    val shapes = kids.map { shape(it.name) }.toSet()
    if (shapes.size != 1) return false
    val parent = parentName.lowercase()
    return shapes.single().split('#')
      .flatMap { part -> part.split(NON_LETTERS) }
      .filter { it.length >= 3 }
      .any { it in parent }
  }

  private fun makeBook(dir: LibraryDir, path: List<String>, kind: BookKind): DetectedBook {
    val flags = LinkedHashSet<BookFlag>()
    val count = dir.audioFileCount
    if (count <= FEW_FILES_MAX) flags += BookFlag.FEW_FILES
    if (count >= MANY_FILES_MIN) flags += BookFlag.MANY_FILES
    if (kind == BookKind.MIXED) flags += BookFlag.MIXED_STRUCTURE
    if (kind == BookKind.SINGLE && isPartName(dir.name) && path.size >= 2) flags += BookFlag.LOOKS_LIKE_PART
    return DetectedBook(dir = dir, pathNames = path.ifEmpty { listOf(dir.name) }, kind = kind, flags = flags)
  }
}
