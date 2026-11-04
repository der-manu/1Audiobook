package voice.data.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import voice.common.BookId
import voice.data.ChapterSortSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Merkt sich pro Buch die gewaehlte Kapitel-Reihenfolge.
 * Ohne Eintrag gilt die bisherige Standard-Reihenfolge.
 * Der Scanner liest diese Einstellung bei jedem Rescan, damit die Wahl nicht ueberschrieben wird.
 */
@Singleton
class ChapterSortStore
@Inject constructor(application: Application) {

  private val prefs: SharedPreferences =
    application.getSharedPreferences("chapter_sort_settings", Context.MODE_PRIVATE)

  fun get(bookId: BookId): ChapterSortSettings? {
    val stored = prefs.getString(bookId.value, null) ?: return null
    return ChapterSortSettings.decode(stored)
  }

  fun set(
    bookId: BookId,
    settings: ChapterSortSettings?,
  ) {
    val editor = prefs.edit()
    if (settings == null) {
      editor.remove(bookId.value)
    } else {
      editor.putString(bookId.value, settings.encode())
    }
    editor.apply()
  }
}
