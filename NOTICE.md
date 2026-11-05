# Notice, credits and licenses

1Audiobook is free software: you can redistribute it and/or modify it under the terms of the
**GNU General Public License, version 3** (see [LICENSE.md](LICENSE.md)). It comes without any warranty.

Copyright of the code lies with its respective authors: Paul Woitaschek and the Voice contributors, Goodwy and the
PlayBook contributors, and the maintainer of 1Audiobook (modifications since the fork). Original copyright notices and
license headers in source files are kept unchanged. Source packages are still named `voice.*` for that reason.

## Lineage and modifications (GPL-3.0 §5)

| Project | Author | License | Relation |
|---|---|---|---|
| [PlayBook](https://github.com/Goodwy/PlayBook) | Goodwy | GPL-3.0 | direct upstream; 1Audiobook is a modified version of PlayBook 4.1.1 |
| [Voice](https://github.com/PaulWoitaschek/Voice) | Paul Woitaschek and contributors | GPL-3.0 | the project PlayBook is based on |

1Audiobook is a **modified version** of PlayBook. All changes are recorded in the git history of this repository
and summarised in [CHANGELOG.md](CHANGELOG.md). The most important ones: removal of all app store connections
(Google Play Billing, in-app review, publishing plugin, RuStore SDK) and of all donation, purchase and "Pro" screens;
library import with an editable preview and `.m3u` support; playlist order dialog with live preview and "Magic" sorting;
quick light/dark switch; hidden-file filtering; replacement of the retired FFmpegKit dependency; new name,
application id and app icon.

The sleep timer improvements are adapted from [PlayBook pull request #159](https://github.com/Goodwy/PlayBook/pull/159)
by Ciske (GPL-3.0), which had not been merged upstream when this app was released.

Translations of the texts that come from Voice and PlayBook were contributed by their communities under the same
license. New texts added by this project exist in English and German.

## Inspiration (no code used)

* **Mort Audiobook Player** by Mirko Schenk (`sto-helit.de`): the idea of loading whole book folders as playlists
  with a sorted folder tree. The program is closed source and unmaintained; nothing of it is included here.

## Components that are part of the app

| Component | Author | License |
|---|---|---|
| [FFmpegKit, maintained fork](https://github.com/ffmpegkit-maintained/ffmpeg) (`dev.ffmpegkit-maintained:ffmpeg-kit-free`); original FFmpegKit by Taner Sener | ffmpegkit-maintained | LGPL-3.0 |
| [FFmpeg](https://ffmpeg.org) (inside FFmpegKit) | FFmpeg developers | LGPL-2.1 or later |
| [AndroidX Media3](https://github.com/androidx/media) (ExoPlayer, Session) | Google | Apache-2.0 |
| [Jetpack Compose](https://developer.android.com/jetpack/compose), Material 3, Material Icons | Google | Apache-2.0 |
| [AndroidX](https://developer.android.com/jetpack/androidx): Core, AppCompat, Activity, Lifecycle, Room, DataStore, Paging, Startup, DocumentFile | Google | Apache-2.0 |
| [Material Components for Android](https://github.com/material-components/material-components-android) | Google | Apache-2.0 |
| [Accompanist](https://github.com/google/accompanist) (System UI Controller) | Google | Apache-2.0 |
| [Core library desugaring](https://github.com/google/desugar_jdk_libs) (`desugar_jdk_libs`), derived from OpenJDK | Google | GPL-2.0 with Classpath Exception |
| [Dagger](https://github.com/google/dagger) | Google | Apache-2.0 |
| [Conductor](https://github.com/bluelinelabs/Conductor) | BlueLine Labs | Apache-2.0 |
| [Navigation Reimagined for Compose](https://github.com/olshevski/compose-navigation-reimagined) | Olshevski | Apache-2.0 |
| [Coil](https://github.com/coil-kt/coil) | Coil contributors | Apache-2.0 |
| [OkHttp](https://square.github.io/okhttp/) and [Retrofit](https://square.github.io/retrofit/) | Square | Apache-2.0 |
| [Seismic](https://github.com/square/seismic) | Square | Apache-2.0 |
| [Material Dialogs](https://github.com/afollestad/material-dialogs) | Aidan Follestad | Apache-2.0 |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines), [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization), [kotlinx.collections.immutable](https://github.com/Kotlin/kotlinx.collections.immutable) | JetBrains | Apache-2.0 |
| Natural string comparison (adapted from [IntelliJ IDEA Community](https://github.com/JetBrains/intellij-community)) | JetBrains | Apache-2.0 |

The authoritative list of dependencies is `gradle/libs.versions.toml` together with the Gradle build files. If you
notice a missing attribution, please open an issue.

## Build and test tools (not part of the app)

Kotlin, Android Gradle Plugin and Gradle (Apache-2.0), [Anvil](https://github.com/square/anvil) (Apache-2.0),
KSP (Apache-2.0), Room compiler (Apache-2.0), ktlint (MIT), JUnit 4 (EPL-1.0), MockK (Apache-2.0), Kotest
(Apache-2.0), Robolectric (MIT), Turbine and Molecule (Apache-2.0), Clikt (Apache-2.0, developer scripts only).

## Online service

The cover search (switched off by default, enabled under *Settings → Cover*) sends the search term you type to the image search of
[DuckDuckGo](https://duckduckgo.com/privacy) and downloads the image you choose from the server that hosts it.
Nothing else leaves the device (see [PRIVACY.md](PRIVACY.md)). 1Audiobook is not affiliated with DuckDuckGo.

## Artwork

The app icon (open book with a play symbol), splash image, in-app logo and notification icon are original artwork
created for this project and are licensed under GPL-3.0 together with the code. The artwork inherited from PlayBook
has been replaced. Other icons are from Material Icons (Apache-2.0).

## AI-assisted development

Most of the changes since the fork were written together with an AI assistant (Claude by Anthropic) and tested by the
maintainer. Code that came from AI assistance is part of this project and is covered by the same license.
