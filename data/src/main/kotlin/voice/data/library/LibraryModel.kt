package voice.data.library

/**
 * Schlanke, von Android unabhaengige Abbildung eines Ordnerbaums.
 * Sie wird einmal beim Scannen aufgebaut und dient der Buch-Erkennung, der Vorschau und der m3u-Zuordnung.
 */
data class LibraryFile(
  val name: String,
  /** Eindeutige Kennung, in der App die Dokument-Adresse als Text. */
  val key: String,
  val lastModified: Long = 0L,
)

data class LibraryDir(
  val name: String,
  /** Eindeutige Kennung, in der App die Dokument-Adresse des Ordners als Text. */
  val key: String,
  val audioFiles: List<LibraryFile>,
  val playlistFiles: List<LibraryFile>,
  val subDirs: List<LibraryDir>,
  /** Anzahl unfertiger Downloads (.part) in genau diesem Ordner. Sie werden ignoriert. */
  val partialFileCount: Int = 0,
) {

  /** Alle Audiodateien in diesem Ordner und allen Unterordnern. */
  val audioFileCount: Int by lazy { audioFiles.size + subDirs.sumOf { it.audioFileCount } }

  val hasAudio: Boolean get() = audioFileCount > 0
}

/** Eine Audiodatei samt Ordnerpfad relativ zu einem Ausgangsordner. */
data class PlacedFile(
  val file: LibraryFile,
  val folders: List<String>,
) {
  val relativePath: String get() = (folders + file.name).joinToString("/")
}

/** Alle Audiodateien des Baums mit relativem Ordnerpfad, in Verzeichnis-Reihenfolge. */
fun LibraryDir.placedAudioFiles(): List<PlacedFile> {
  val result = ArrayList<PlacedFile>()
  fun collect(dir: LibraryDir, folders: List<String>) {
    dir.audioFiles.forEach { result += PlacedFile(it, folders) }
    dir.subDirs.forEach { collect(it, folders + it.name) }
  }
  collect(this, emptyList())
  return result
}

/** Alle Ordner des Baums (inklusive dieses) mit relativem Pfad. */
fun LibraryDir.allDirs(): List<Pair<List<String>, LibraryDir>> {
  val result = ArrayList<Pair<List<String>, LibraryDir>>()
  fun collect(dir: LibraryDir, folders: List<String>) {
    result += folders to dir
    dir.subDirs.forEach { collect(it, folders + it.name) }
  }
  collect(this, emptyList())
  return result
}
