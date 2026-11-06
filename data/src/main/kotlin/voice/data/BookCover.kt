package voice.data

import java.io.File

/**
 * Das Cover, das fuer ein Buch angezeigt wird: das Cover des aktuellen Kapitels, wenn der Nutzer es wuenscht und das
 * Kapitel eines hat, sonst das Cover des Buchs. Fehlt dem Kapitel ein eigenes Cover (bei Ordner-Buechern der Normalfall),
 * wird so nicht "kein Cover" angezeigt, sondern das gewaehlte Buch-Cover.
 */
fun Book.displayCover(useChapterCoverSetting: Boolean): File? {
  val chapterCover = if (content.useChapterCover && useChapterCoverSetting) currentChapter.cover else null
  return chapterCover ?: content.cover
}
