# Notice, credits and licenses

PlayBookAdvance is free software: you can redistribute it and/or modify it under the terms of the
**GNU General Public License, version 3** (see [LICENSE](LICENSE)). It comes without any warranty.

## Lineage and modifications (GPL-3.0 §5)

| Project | Author | License | Relation |
|---|---|---|---|
| [PlayBook](https://github.com/Goodwy/PlayBook) | Goodwy | GPL-3.0 | direct upstream; PlayBookAdvance is a modified version of it |
| [Voice](https://github.com/PaulWoitaschek/Voice) | Paul Woitaschek and contributors | GPL-3.0 | the project PlayBook is based on |

PlayBookAdvance is a **modified version** of PlayBook 4.1.1. The changes are recorded in the git history of this
repository. The most important ones: removal of the RuStore SDK, Google Play Billing, and all donation / purchase
screens and the Pro lock (all features unlocked); new playlist order dialog with live preview; quick light/dark
switch; new app icon; replacement of the retired FFmpegKit dependency; new application id and name. Original copyright notices in the source files are unchanged.

The app icon (open book with a play symbol), splash image, in-app logo and notification icon are original artwork
created for PlayBookAdvance and are licensed under GPL-3.0 together with the code. The artwork inherited from
PlayBook has been replaced.

## Inspiration (no code used)

* **Mort Audiobook Player** by Mirko Schenk (`sto-helit.de`): the idea of loading whole book folders as playlists
  with a sorted folder tree. The program is closed source and unmaintained; nothing of it is included here.

## Third-party components

| Component | Author | License |
|---|---|---|
| [FFmpegKit, maintained fork](https://github.com/ffmpegkit-maintained/ffmpeg) (`dev.ffmpegkit-maintained:ffmpeg-kit-free`) | ffmpegkit-maintained; original FFmpegKit by Taner Sener | LGPL-3.0 |
| [FFmpeg](https://ffmpeg.org) (inside FFmpegKit) | FFmpeg developers | LGPL-2.1 or later |
| [AndroidX Media3 / ExoPlayer](https://github.com/androidx/media) | Google | Apache-2.0 |
| Jetpack Compose, Material 3, Room, DataStore, AppCompat, Paging and other [AndroidX](https://developer.android.com/jetpack) libraries | Google | Apache-2.0 |
| [Dagger](https://github.com/google/dagger) | Google | Apache-2.0 |
| [Conductor](https://github.com/bluelinelabs/Conductor) | BlueLine Labs | Apache-2.0 |
| [Coil](https://github.com/coil-kt/coil) | Coil contributors | Apache-2.0 |
| [Lottie for Android](https://github.com/airbnb/lottie-android) | Airbnb | Apache-2.0 |
| [OkHttp](https://square.github.io/okhttp/), [Retrofit](https://square.github.io/retrofit/), [Seismic](https://github.com/square/seismic) | Square | Apache-2.0 |
| [Material Dialogs](https://github.com/afollestad/material-dialogs) | Aidan Follestad | Apache-2.0 |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines), [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization), [kotlinx.collections.immutable](https://github.com/Kotlin/kotlinx.collections.immutable) | JetBrains | Apache-2.0 |
| Natural string comparison (adapted from [IntelliJ IDEA Community](https://github.com/JetBrains/intellij-community)) | JetBrains | Apache-2.0 |
| [LeakCanary](https://github.com/square/leakcanary) (debug builds only) | Square | Apache-2.0 |

The authoritative list of dependencies is `gradle/libs.versions.toml`. If you notice a missing attribution,
please open an issue.

## Online service

The optional cover search sends the search term you type to the image search of
[DuckDuckGo](https://duckduckgo.com/privacy) and downloads the image you choose from the server that hosts it.
Nothing else leaves the device (see [PRIVACY.md](PRIVACY.md)).
