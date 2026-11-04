package voice.data.library

import android.content.Context
import android.net.Uri
import voice.logging.core.Logger
import javax.inject.Inject

/** Liest den Inhalt einer Playlist-Datei ueber den Content-Resolver. */
class PlaylistReader
@Inject constructor(private val context: Context) {

  /** @return die Bytes der Datei oder null, wenn sie nicht lesbar oder unplausibel gross ist. */
  fun read(file: LibraryFile): ByteArray? {
    return try {
      context.contentResolver.openInputStream(Uri.parse(file.key))?.use { it.readBytes() }
        ?.takeIf { it.size <= MAX_BYTES }
    } catch (e: Exception) {
      Logger.w("Could not read playlist ${file.name}")
      null
    }
  }

  private companion object {
    const val MAX_BYTES = 2_000_000
  }
}
