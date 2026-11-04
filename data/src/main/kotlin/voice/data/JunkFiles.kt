package voice.data

import voice.documentfile.CachedDocumentFile

/**
 * Versteckte Dateien und Ordner gehoeren nie zu einem Buch. Dazu zaehlt der Beifang beim Kopieren von macOS:
 * Dateien wie "._028 - Kapitel 028.mp3" (Metadaten zu "028 - Kapitel 028.mp3", keine Audiodaten) und der Ordner
 * "__MACOSX" aus entpackten Archiven. Die Endung ".mp3" taeuscht dort ein Audiofile vor.
 */
fun isJunkName(name: String?): Boolean {
  return name != null && (name.startsWith(".") || name == "__MACOSX")
}

fun CachedDocumentFile.isJunk(): Boolean = isJunkName(name)
