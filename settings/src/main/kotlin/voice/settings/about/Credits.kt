package voice.settings.about

/**
 * Alle Projekte, auf denen 1Audiobook aufbaut. Wird im "Ueber"-Bildschirm angezeigt.
 * Bitte hier UND in NOTICE.md ergaenzen, sobald eine Bibliothek oder Quelle dazukommt.
 */
internal data class Credit(
  val name: String,
  val detail: String,
  val url: String?,
)

internal object Links {
  const val PROJECT = "https://github.com/der-manu/1Audiobook"
  const val ISSUES = "https://github.com/der-manu/1Audiobook/issues/new"
  const val PRIVACY = "https://github.com/der-manu/1Audiobook/blob/master/PRIVACY.md"
}

internal object Credits {

  /** Direkte Vorlagen, von denen dieses Projekt abstammt. */
  val basedOn = listOf(
    Credit(
      name = "PlayBook",
      detail = "Goodwy \u00B7 GPL-3.0 \u00B7 direct upstream of this app",
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

  /** Bibliotheken, die in der App stecken. Test- und Build-Werkzeuge stehen nur in NOTICE.md. */
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
      name = "AndroidX Media3 (ExoPlayer, Session)",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://github.com/androidx/media",
    ),
    Credit(
      name = "Jetpack Compose, Material 3 and Material Icons",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://developer.android.com/jetpack/compose",
    ),
    Credit(
      name = "AndroidX Core, AppCompat, Activity, Lifecycle, Room, DataStore, Paging, Startup, DocumentFile",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://developer.android.com/jetpack/androidx",
    ),
    Credit(
      name = "Material Components for Android",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://github.com/material-components/material-components-android",
    ),
    Credit(
      name = "Accompanist (System UI Controller)",
      detail = "Google \u00B7 Apache-2.0",
      url = "https://github.com/google/accompanist",
    ),
    Credit(
      name = "Android Core Library Desugaring (desugar_jdk_libs)",
      detail = "Google, derived from OpenJDK \u00B7 GPL-2.0 with Classpath Exception",
      url = "https://github.com/google/desugar_jdk_libs",
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
      name = "Navigation Reimagined for Compose",
      detail = "Olshevski \u00B7 Apache-2.0",
      url = "https://github.com/olshevski/compose-navigation-reimagined",
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
      name = "Seismic",
      detail = "Square \u00B7 Apache-2.0",
      url = "https://github.com/square/seismic",
    ),
    Credit(
      name = "Material Dialogs",
      detail = "Aidan Follestad \u00B7 Apache-2.0",
      url = "https://github.com/afollestad/material-dialogs",
    ),
    Credit(
      name = "kotlinx.coroutines, kotlinx.serialization, kotlinx.collections.immutable",
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
