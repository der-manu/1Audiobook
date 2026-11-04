package voice.data

import voice.documentfile.CachedDocumentFile

val supportedAudioFormats = arrayOf(
  "3gp",
  "aac",
  "awb",
  "flac",
  "imy",
  "m4a",
  "m4b",
  "mid",
  "mka",
  "mkv",
  "mp3",
  "mp3package",
  "mp4",
  "mpga",
  "opus",
  "mxmf",
  "oga",
  "ogg",
  "ota",
  "rtttl",
  "rtx",
  "wav",
  "webm",
  "xmf",
)

fun CachedDocumentFile.isAudioFile(): Boolean {
  if (!isFile || isJunk()) return false
  val name = name ?: return false
  val extension = name.substringAfterLast(".").lowercase()
  return extension in supportedAudioFormats
}

fun CachedDocumentFile.audioFileCount(): Int {
  return when {
    isJunk() -> 0
    isAudioFile() -> 1
    isDirectory -> children.sumOf { it.audioFileCount() }
    else -> 0
  }
}
