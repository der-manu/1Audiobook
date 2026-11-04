package voice.data.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import voice.common.BookId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Merkt sich Buchtitel, die der Nutzer beim Bibliotheks-Import vergeben oder bestaetigt hat.
 * Der Scanner nutzt sie beim ersten Anlegen eines Buchs, bevor er auf Tags oder den Ordnernamen zurueckgreift.
 */
@Singleton
class BookNameStore
@Inject constructor(application: Application) {

  private val prefs: SharedPreferences =
    application.getSharedPreferences("book_names", Context.MODE_PRIVATE)

  fun get(bookId: BookId): String? = prefs.getString(bookId.value, null)?.takeIf { it.isNotBlank() }

  fun setAll(names: Map<BookId, String>) {
    val editor = prefs.edit()
    names.forEach { (id, name) ->
      if (name.isBlank()) editor.remove(id.value) else editor.putString(id.value, name.trim())
    }
    editor.apply()
  }
}
