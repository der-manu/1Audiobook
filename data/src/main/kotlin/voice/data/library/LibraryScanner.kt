package voice.data.library

import voice.data.isJunkName
import voice.data.supportedAudioFormats
import voice.documentfile.CachedDocumentFile
import voice.documentfile.nameWithoutExtension
import javax.inject.Inject

/**
 * Liest einen Ordnerbaum einmal komplett und baut daraus das schlanke [LibraryDir]-Modell.
 * Versteckte Dateien und macOS-Beifang (siehe [isJunkName]) werden uebersprungen, .part-Dateien nur gezaehlt.
 */
class LibraryScanner
@Inject constructor() {

  /** @param onFile wird fuer jede gelesene Datei aufgerufen (Fortschrittsanzeige). */
  fun scan(
    root: CachedDocumentFile,
    onFile: () -> Unit = {},
  ): LibraryDir {
    val audio = ArrayList<LibraryFile>()
    val playlists = ArrayList<LibraryFile>()
    val dirs = ArrayList<LibraryDir>()
    var partial = 0
    for (child in root.children) {
      val name = child.name ?: continue
      if (isJunkName(name)) continue
      if (child.isDirectory) {
        dirs += scan(child, onFile)
      } else if (child.isFile) {
        onFile()
        val extension = name.substringAfterLast('.', "").lowercase()
        val file = LibraryFile(name = name, key = child.uri.toString(), lastModified = child.lastModified)
        when {
          extension in supportedAudioFormats -> audio += file
          extension == "m3u" || extension == "m3u8" -> playlists += file
          extension == "part" -> partial++
        }
      }
    }
    return LibraryDir(
      // Bei Ordnern gilt der volle Name: "Teil 1. Der Anfang" hat keine Dateiendung.
      name = root.name ?: root.nameWithoutExtension(),
      key = root.uri.toString(),
      audioFiles = audio,
      playlistFiles = playlists,
      subDirs = dirs,
      partialFileCount = partial,
    )
  }
}
