package voice.data.library

import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.text.Normalizer
import kotlin.math.ceil

/** Liest .m3u / .m3u8. Alte Dateien sind oft in Windows-1252 statt UTF-8 gespeichert. */
object PlaylistParser {

  fun decode(bytes: ByteArray): String {
    var data = bytes
    if (data.size >= 3 && data[0] == 0xEF.toByte() && data[1] == 0xBB.toByte() && data[2] == 0xBF.toByte()) {
      data = data.copyOfRange(3, data.size)
    }
    return try {
      Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(data))
        .toString()
    } catch (e: CharacterCodingException) {
      String(data, Charset.forName("windows-1252"))
    }
  }

  /** Alle Dateieintraege in Reihenfolge, ohne Kommentare (#EXTM3U, #EXTINF) und Leerzeilen. */
  fun entries(text: String): List<String> {
    return text.lineSequence()
      .map { it.trim().trim('\uFEFF') }
      .filter { it.isNotEmpty() && !it.startsWith("#") }
      .map(::decodeEntry)
      .toList()
  }

  private fun decodeEntry(entry: String): String {
    if (!entry.startsWith("file:", ignoreCase = true)) return entry
    val withoutScheme = entry.removePrefix("file:").removePrefix("FILE:").trimStart('/')
    return try {
      URLDecoder.decode(withoutScheme.replace("+", "%2B"), "UTF-8")
    } catch (e: IllegalArgumentException) {
      withoutScheme
    }
  }
}

data class PlaylistMatch(
  /** Dateikennung -> Position in der Playlist (0, 1, 2, ...). */
  val rankByKey: Map<String, Int>,
  val entryCount: Int,
  val matchedEntries: Int,
  val unmatchedEntries: List<String>,
)

/** Ordnet Playlist-Eintraege den echten Dateien zu: tolerant bei Pfaden, Gross-/Kleinschreibung und Umlauten. */
object PlaylistMatcher {

  fun match(entries: List<String>, files: List<PlacedFile>): PlaylistMatch {
    val byName = HashMap<String, MutableList<Int>>()
    val byStem = HashMap<String, MutableList<Int>>()
    val fileFolderKeys = files.map { f -> f.folders.map(::keys) }
    files.forEachIndexed { index, f ->
      keys(f.file.name).forEach { byName.getOrPut(it) { ArrayList() } += index }
      keys(stem(f.file.name)).forEach { byStem.getOrPut(it) { ArrayList() } += index }
    }

    val used = BooleanArray(files.size)
    val ranks = LinkedHashMap<String, Int>()
    val unmatched = ArrayList<String>()
    var counted = 0
    for (entry in entries) {
      val segments = splitEntry(entry)
      if (segments.isEmpty()) continue
      counted++
      val base = segments.last()
      val entryFolders = segments.dropLast(1).map(::keys)

      var best = pick(byName, keys(base), used, entryFolders, fileFolderKeys)
      if (best < 0) best = pick(byStem, keys(stem(base)), used, entryFolders, fileFolderKeys)
      if (best >= 0) {
        used[best] = true
        ranks[files[best].file.key] = ranks.size
      } else {
        unmatched += entry
      }
    }
    return PlaylistMatch(ranks, counted, ranks.size, unmatched)
  }

  private fun pick(
    index: Map<String, List<Int>>,
    nameKeys: Set<String>,
    used: BooleanArray,
    entryFolders: List<Set<String>>,
    fileFolderKeys: List<List<Set<String>>>,
  ): Int {
    var best = -1
    var bestScore = -1
    for (key in nameKeys) {
      for (candidate in index[key].orEmpty()) {
        if (used[candidate]) continue
        val score = commonTrailingFolders(entryFolders, fileFolderKeys[candidate])
        if (score > bestScore || (score == bestScore && candidate < best)) {
          best = candidate
          bestScore = score
        }
      }
    }
    return best
  }

  private fun commonTrailingFolders(entry: List<Set<String>>, file: List<Set<String>>): Int {
    var score = 0
    var e = entry.lastIndex
    var f = file.lastIndex
    while (e >= 0 && f >= 0 && entry[e].intersect(file[f]).isNotEmpty()) {
      score++
      e--
      f--
    }
    return score
  }

  private fun splitEntry(entry: String): List<String> {
    return entry.replace('\\', '/')
      .split('/')
      .map { it.trim() }
      .filter { it.isNotEmpty() && it != "." && it != ".." && !(it.length == 2 && it[1] == ':') }
  }

  private fun stem(name: String): String = name.substringBeforeLast('.', name)

  /** Vergleichsschluessel: klein, ohne Akzente, ohne Sonderzeichen; "ae" und "ä" gelten als gleich. */
  private fun keys(value: String): Set<String> {
    val lower = value.lowercase()
    val translit = lower.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
    return setOf(normalize(lower), normalize(translit))
  }

  private fun normalize(value: String): String {
    val decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
    val sb = StringBuilder(decomposed.length)
    for (c in decomposed) {
      if (Character.getType(c) == Character.NON_SPACING_MARK.toInt()) continue
      if (c.isLetterOrDigit()) sb.append(c)
    }
    return sb.toString()
  }
}

enum class PlaylistMode {
  /** Keine brauchbare Playlist. */
  NONE,

  /** Eine Playlist im Buchordner bestimmt die Reihenfolge ueber alle Dateien (auch ueber Unterordner). */
  GLOBAL,

  /** Playlists in einzelnen CD-Ordnern bestimmen die Reihenfolge innerhalb ihres Ordners. */
  PER_DIR,
}

data class PlaylistPlan(
  val mode: PlaylistMode,
  val rankByKey: Map<String, Int>,
  val playlistNames: List<String>,
  val entryCount: Int,
  val matchedEntries: Int,
  /** Audiodateien im Geltungsbereich, die in der Playlist fehlen. Sie kommen ans Ende. */
  val unlistedFiles: Int,
  /** Es gibt Playlists, aber keine ist brauchbar (zu viele Eintraege ohne Datei). */
  val defective: Boolean,
) {
  val usable: Boolean get() = mode != PlaylistMode.NONE

  companion object {
    val NONE = PlaylistPlan(PlaylistMode.NONE, emptyMap(), emptyList(), 0, 0, 0, false)
  }
}

object PlaylistPlanner {

  /** So viel Anteil von Eintraegen und von Dateien muss zugeordnet werden, damit die Playlist gilt. */
  const val MIN_RATIO = 0.6

  fun isUsable(match: PlaylistMatch, scopeFileCount: Int): Boolean {
    if (match.entryCount == 0 || scopeFileCount == 0) return false
    fun need(n: Int) = ceil(MIN_RATIO * n).toInt().coerceAtLeast(1)
    return match.matchedEntries >= need(match.entryCount) && match.matchedEntries >= need(scopeFileCount)
  }

  /**
   * Prueft die Playlists eines Buchs. Zuerst zaehlt eine Playlist im Buchordner (ueber alle Dateien),
   * sonst werden Playlists in einzelnen Unterordnern fuer deren Dateien genutzt.
   *
   * @param read liefert den Inhalt einer Playlist-Datei oder null, wenn sie nicht lesbar ist.
   */
  fun plan(book: LibraryDir, read: (LibraryFile) -> ByteArray?): PlaylistPlan {
    val anyPlaylist = book.allDirs().any { it.second.playlistFiles.isNotEmpty() }
    if (!anyPlaylist) return PlaylistPlan.NONE

    val allFiles = book.placedAudioFiles()
    for (playlist in book.playlistFiles.sortedBy { it.name.lowercase() }) {
      val match = matchOf(playlist, allFiles, read) ?: continue
      if (isUsable(match, allFiles.size)) {
        return PlaylistPlan(
          mode = PlaylistMode.GLOBAL,
          rankByKey = match.rankByKey,
          playlistNames = listOf(playlist.name),
          entryCount = match.entryCount,
          matchedEntries = match.matchedEntries,
          unlistedFiles = allFiles.size - match.matchedEntries,
          defective = false,
        )
      }
    }

    val ranks = LinkedHashMap<String, Int>()
    val names = ArrayList<String>()
    var entries = 0
    var matched = 0
    var scope = 0
    for ((_, dir) in book.allDirs()) {
      if (dir.playlistFiles.isEmpty() || dir.audioFiles.isEmpty()) continue
      val placed = dir.audioFiles.map { PlacedFile(it, emptyList()) }
      for (playlist in dir.playlistFiles.sortedBy { it.name.lowercase() }) {
        val match = matchOf(playlist, placed, read) ?: continue
        if (isUsable(match, placed.size)) {
          ranks.putAll(match.rankByKey)
          names += playlist.name
          entries += match.entryCount
          matched += match.matchedEntries
          scope += placed.size
          break
        }
      }
    }
    if (ranks.isEmpty()) return PlaylistPlan.NONE.copy(defective = true)
    return PlaylistPlan(
      mode = PlaylistMode.PER_DIR,
      rankByKey = ranks,
      playlistNames = names,
      entryCount = entries,
      matchedEntries = matched,
      unlistedFiles = scope - matched,
      defective = false,
    )
  }

  private fun matchOf(
    playlist: LibraryFile,
    files: List<PlacedFile>,
    read: (LibraryFile) -> ByteArray?,
  ): PlaylistMatch? {
    val bytes = read(playlist) ?: return null
    val entries = PlaylistParser.entries(PlaylistParser.decode(bytes))
    return PlaylistMatcher.match(entries, files)
  }
}
