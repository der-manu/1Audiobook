package voice.settings.about

/**
 * Alle Projekte, auf denen PlayBookAdvance aufbaut. Wird im "Ueber"-Bildschirm angezeigt.
 * Bitte hier ergaenzen, sobald eine neue Bibliothek oder Quelle dazukommt.
 * Dieselbe Liste steht (ausfuehrlicher) in NOTICE.md.
 */
internal data class Credit(
  val name: String,
  val detail: String,
  val url: String?,
)

internal object Links {
  const val PROJECT = "https://github.com/der-manu/PlayBookAdvance"
  const val ISSUES = "https://github.com/der-manu/PlayBookAdvance/issues/new"
  const val PRIVACY = "https://github.com/der-manu/PlayBookAdvance/blob/master/PRIVACY.md"
}

internal object Credits {

  /** Direkte Vorlagen, von denen dieses Projekt abstammt. */
  val basedOn = listOf(
    Credit(
      name = "PlayBook",
      detail = "Goodwy \u00B7 GPL-3.0 \u00B7 direct upstream of this fork",
      url = "https://github.com/Goodwy/PlayBook",
    ),
    Credit(
      name = "Voice",
      detail = "Paul Woitaschek and contributors \u00B7 GPL-3.0 \u00B7 the project PlayBook is based on",
      url = "https://github.com/PaulWoitaschek/Voice",
    ),
  )

  /** Ideen, aus denen kein Code uebernommen wurde. */
  val inspiration = listOf(
    Credit(
      name = "Mort Audiobook Player",
      detail = "Mirko Schenk \u00B7 inspiration for folder-based playlists (idea only, no code used)",
      url = null,
    ),
  )

  val libraries = listOf(
    Credit(
      name = "FFmpegKit (maintained fork)",
      detail = "ffmpegkit-maintained \u00B7 LGPL-3.0 \u00B7 continuation of FFmpegKit by Taner Sener",
      url = "https://github.com/ffmpegkit-maintained/ffmpeg",
    ),
    Credit(
      name = "FFmpeg",
      detail = "FFmpeg developers \u00B7 LGPL-2.1+",
      url = "https://ffmpeg.org",
    ),
    Credit(
      name = "AndroidX Media3 (ExoPlayer)",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://github.com/androidx/media",
    ),
    Credit(
      name = "Jetpack Compose, Material 3, Room, DataStore and other AndroidX libraries",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://developer.android.com/jetpack",
    ),
    Credit(
      name = "Dagger",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://github.com/google/dagger",
    ),
    Credit(
      name = "Conductor",
      detail = "BlueLine Labs \u00B7 Apache-2.0",
      url = "https://github.com/bluelinelabs/Conductor",
    ),
    Credit(
      name = "Coil",
      detail = "Coil contributors \u00B7 Apache-2.0",
      url = "https://github.com/coil-kt/coil",
    ),
    Credit(
      name = "OkHttp and Retrofit",
      detail = "Square \u00B7 Apache-2.0",
      url = "https://square.github.io/okhttp/",
    ),
    Credit(
      name = "Material Dialogs",
      detail = "Aidan Follestad \u00B7 Apache-2.0",
      url = "https://github.com/afollestad/material-dialogs",
    ),
    Credit(
      name = "kotlinx.coroutines and kotlinx.serialization",
      detail = "JetBrains \u00B7 Apache-2.0",
      url = "https://github.com/Kotlin/kotlinx.coroutines",
    ),
    Credit(
      name = "IntelliJ IDEA Community (natural string comparison)",
      detail = "JetBrains \u00B7 Apache-2.0",
      url = "https://github.com/JetBrains/intellij-community",
    ),
    Credit(
      name = "DuckDuckGo image search",
      detail = "Used by the optional cover search \u00B7 search terms you type are sent to DuckDuckGo",
      url = "https://duckduckgo.com/privacy",
    ),
  )
}
