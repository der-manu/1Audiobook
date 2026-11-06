package voice.app.scanner

import android.content.Context
import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import voice.common.BookId
import voice.common.pref.PrefKeys
import voice.data.toUri
import voice.logging.core.Logger
import voice.pref.Pref
import javax.inject.Inject
import javax.inject.Named

/**
 * Schreibt ein vom Nutzer gewaehltes Cover zusaetzlich als "cover.png" in den Ordner des Buchs, wenn der Nutzer das in
 * den Einstellungen erlaubt hat. Der Scanner nimmt das erste Bild im Buchordner als Cover. Die Datei ueberlebt
 * deshalb eine Neuinstallation und erscheint auch in anderen Playern.
 *
 * Es wird nur die eigene Datei "cover.png" ersetzt. Alle anderen Dateien bleiben unangetastet.
 */
class CoverFolderWriter
@Inject constructor(
  private val context: Context,
  @Named(PrefKeys.SAVE_COVER_TO_FOLDER)
  private val enabled: Pref<Boolean>,
) {

  enum class WriteResult {
    /** Die Einstellung ist aus. */
    DISABLED,

    /** Kein Ordner zum Beschreiben da (Einzeldatei-Buch). */
    SKIPPED,

    WRITTEN,

    /** Kein Schreibrecht oder ein Fehler beim Schreiben. */
    FAILED,
  }

  suspend fun writeIfEnabled(
    bookId: BookId,
    cover: Bitmap,
  ): WriteResult = withContext(Dispatchers.IO) {
    if (!enabled.value) return@withContext WriteResult.DISABLED
    try {
      val folder = DocumentFile.fromTreeUri(context, bookId.toUri())
      if (folder == null || !folder.isDirectory) return@withContext WriteResult.SKIPPED
      if (!folder.canWrite()) return@withContext WriteResult.FAILED

      folder.findFile(FILE_NAME)?.delete()
      val file = folder.createFile("image/png", FILE_BASE_NAME) ?: return@withContext WriteResult.FAILED
      val written = context.contentResolver.openOutputStream(file.uri)?.use { output ->
        cover.compress(Bitmap.CompressFormat.PNG, 100, output)
      } ?: false
      if (written) WriteResult.WRITTEN else WriteResult.FAILED
    } catch (e: Exception) {
      Logger.w(e, "Could not save the cover in the book folder")
      WriteResult.FAILED
    }
  }

  private companion object {
    const val FILE_BASE_NAME = "cover"
    const val FILE_NAME = "cover.png"
  }
}
