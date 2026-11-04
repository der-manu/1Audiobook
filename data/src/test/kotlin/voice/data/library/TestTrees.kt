package voice.data.library

/** Baut kleine Ordnerbaeume fuer Tests: dir("Buch", files("01.mp3"), dir("CD1", ...)). */
internal fun file(name: String) = LibraryFile(name = name, key = name)

internal sealed interface Entry
internal data class FileEntry(val name: String) : Entry
internal data class DirEntry(val dir: LibraryDir) : Entry

internal fun audio(vararg names: String): List<Entry> = names.map { FileEntry(it) }

internal fun dir(name: String, vararg entries: Entry, key: String = name): LibraryDir {
  val files = entries.filterIsInstance<FileEntry>().map { it.name }
  val audioExtensions = setOf("mp3", "m4a", "m4b", "ogg", "flac", "wav", "opus")
  return LibraryDir(
    name = name,
    key = key,
    audioFiles = files.filter { it.substringAfterLast('.', "").lowercase() in audioExtensions }
      .map { LibraryFile(it, "$key/$it") },
    playlistFiles = files.filter { it.endsWith(".m3u") || it.endsWith(".m3u8") }
      .map { LibraryFile(it, "$key/$it") },
    subDirs = entries.filterIsInstance<DirEntry>().map { it.dir },
  )
}

internal fun d(dir: LibraryDir) = DirEntry(dir)
internal fun f(vararg names: String): Array<Entry> = names.map { FileEntry(it) as Entry }.toTypedArray()

internal fun numbered(count: Int, prefix: String = "") = (1..count).map { FileEntry("$prefix%02d.mp3".format(it)) as Entry }.toTypedArray()
