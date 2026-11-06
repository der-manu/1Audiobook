# 1Audiobook

**Local first. Import folders containing audiobooks. Play offline.**
*Inspired by PlayBook and Mort Player Audiobooks. Vibe coded.*

1Audiobook is a free, open-source audiobook player for Android (GPL-3.0). It plays the audio files that are already
on your phone. There is no account, no cloud and no store connection. Point it at a folder, check the preview, and
your library is ready.

> **Status:** release candidate. Expect rough edges and please
> [report problems](https://github.com/der-manu/1Audiobook/issues).

## Why this app exists

The **Mort Audiobook Player** by Mirko Schenk gave the first spark. Mort read folders the way an audiobook listener
thinks: a whole book folder, with its sub-folders, was loaded as **one playlist in the right order**, sorted by tags or
alphabetically, with a **preview** of the result and a folder tree with covers. Mort is closed source, no longer
developed, and does not run reliably on current Android versions.

1Audiobook brings that idea back on a modern base. It is built on PlayBook, a current audiobook player, and rebuilds the
folder reading from scratch: **no code and no assets of Mort were used, only the idea.**

## Features

- **Import a whole library.** Choose a folder; books in folders, multi-CD books (`CD1`, `CD2`, `Disc 2`, `Teil 3` …),
  author and series folders are recognised automatically. Release-style folder names such as
  `Karen.Test.-.2011.-.Todesstrafe.Germ.wOOt` become readable titles (*Karen Test - 2011 - Todesstrafe*).
  **Nothing is added before you confirm** an editable preview (rename, deselect, change the order, see which books
  need a look).
- **Playlists are respected.** A usable `.m3u` decides the order; a broken one is detected and replaced by sorting.
  The preview shows how well it matches and lets you replace it.
- **Playlist order with live preview** (player screen, `⋯` menu → *Playlist order…*): group by folder (folder 1 = 1…N,
  then folder 2 = 1…N) or sort all files together, by file name, tag title or date, optionally reversed. *Magic*
  recognises CD numbers inside file names (`01 - Title (CD1).mp3`) and puts CD 1 completely before CD 2. Long file
  names can be read by swiping the list sideways. The preview updates immediately; the choice is remembered per book
  and survives rescans.
- **Cover search that explains itself** (optional, off by default): shorter suggestions made from title and author,
  clear error messages, and fallbacks to search in your browser or paste an image link. A chosen cover can optionally
  also be saved as `cover.png` in the book folder (off by default).
- **Plays offline.** Resume where you stopped, sleep timer (restarts when you interact, lets you continue during the
  fade-out), bookmarks, playback speed, volume boost, chapters
  (for example m4b), media controls on the lock screen and with headsets, home-screen widget.
- **Clean files only.** Hidden files and macOS leftovers (`._01.mp3`, `__MACOSX`) never end up in a playlist.
- **Light and dark theme** with a quick switch at the top of *Settings → Appearance* (the *Theme* dialog still offers
  *Follow system*).
- **Offline by default.** No ads, no tracking, no accounts, no in-app purchases. The online cover search must be
  switched on by you. All features are free.

## Compared with PlayBook

1Audiobook is derived from [PlayBook](https://github.com/Goodwy/PlayBook). The differences:

- Library import, `.m3u` support, playlist order dialog with live preview and *Magic* sorting, hidden-file filtering.
- Explanatory, optional cover search; quick light/dark switch; new name, application id and app icon.
- **No app store connections:** Google Play Billing, in-app review and the Play publishing plugin are gone, and so is
  the RuStore SDK. There is no paywall, no donation screen and no purchase of any kind.
- The retired FFmpegKit dependency was replaced by a maintained fork. Because that fork only ships a 64-bit ARM
  library, **only `arm64-v8a` devices are supported**.

The full list is in [CHANGELOG.md](CHANGELOG.md).

## Install

Download the APK of the latest release from [Releases](https://github.com/der-manu/1Audiobook/releases) and allow
installation from your browser or file manager. An F-Droid listing is planned (see [docs/FDROID.md](docs/FDROID.md)).

Requirements: Android 8.0 (API 26) or newer on a **64-bit ARM** device.

## Privacy

The app works fully offline. The only feature that uses the internet is the **cover search**, which is **off by
default**: switch on *Settings → Cover → Search missing covers via DuckDuckGo* and the book title is sent to
DuckDuckGo's image search when you use *Cover from the Internet*. Details: [PRIVACY.md](PRIVACY.md).

## Build

Requires JDK 17 and the Android SDK.

```bash
./gradlew assembleDebug      # debug APK in app/build/outputs/apk/debug/
./gradlew assembleRelease    # unsigned release APK unless the signing variables are set
```

How releases are signed and published: [docs/RELEASE.md](docs/RELEASE.md).

## Origin, credits and license

1Audiobook is licensed under the [GNU General Public License v3.0](LICENSE.md). It is derived from
[PlayBook](https://github.com/Goodwy/PlayBook) by Goodwy, which is based on
[Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek. The original copyright notices are kept, and
source code packages are still named `voice.*` for that reason. Every project and library the app is built on is
listed with its license in [NOTICE.md](NOTICE.md) and in the app's *About* screen.

**Inspiration:** the Mort Audiobook Player by Mirko Schenk (see *Why this app exists*). No Mort code was used.

### AI-assisted development ("vibe coded")

Most of the changes since the fork were written together with an AI assistant (Claude by Anthropic) and tested by the
maintainer on a real device. The code is open, so please review it before you rely on it. There is no warranty.

---

## Auf Deutsch

**Local first. Ordner mit Hörbüchern importieren. Offline hören.**
*Inspiriert von PlayBook und Mort Player Audiobooks. Vibe coded.*

1Audiobook ist ein freier Hörbuchplayer für Android (GPL-3.0). Er spielt die Audiodateien, die schon auf dem Handy
liegen. Es gibt kein Konto, keine Cloud und keine Store-Anbindung.

**Warum es diese App gibt:** Den ersten Anstoß gab der **Mort Audiobook Player** von Mirko Schenk. Mort las Ordner so,
wie ein Hörbuch-Hörer denkt: Ein ganzer Buchordner samt Unterordnern wurde als **eine Playlist in der richtigen
Reihenfolge** geladen, sortiert nach Tags oder alphabetisch, mit **Vorschau** und Ordnerbaum samt Covern. Mort ist
closed source, wird nicht mehr weiterentwickelt und läuft auf aktuellen Android-Versionen nicht zuverlässig.
1Audiobook greift diese Idee auf einer modernen Basis (PlayBook) wieder auf und baut das Einlesen von Ordnern neu:
**Es wurde kein Code und kein Material von Mort verwendet, nur die Idee.**

- **Ganze Bibliothek importieren:** Ordner wählen, Bücher (auch Mehr-CD-Bücher wie `CD1`/`CD2`, Autoren- und
  Reihen-Ordner) werden erkannt, Namen aus Release-Ordnern bereinigt. **Erst nach deiner Bestätigung** einer
  bearbeitbaren Vorschau wird etwas hinzugefügt.
- **m3u-Playlists** bestimmen die Reihenfolge, wenn sie brauchbar sind. Defekte werden erkannt und ersetzt.
- **Reihenfolge mit Live-Vorschau**, inklusive *Zaubern* für CD-Nummern in Dateinamen. Lange Dateinamen lassen sich
  seitlich wischen.
- **Cover-Suche, die sich erklärt** (optional, standardmäßig aus): kürzere Suchvorschläge, klare Fehlermeldungen und
  Ausweichwege über den Browser oder einen eingefügten Bild-Link.
- **Offline abspielen:** Position merken, Schlaf-Timer, Lesezeichen, Geschwindigkeit, Lautstärke-Boost, Kapitel,
  Sperrbildschirm- und Headset-Steuerung, Widget.
- **Standardmäßig offline.** Keine Werbung, kein Tracking, keine Konten, keine Käufe. Die Online-Cover-Suche
  (DuckDuckGo) musst du in den Einstellungen selbst einschalten.

Installation: APK aus den [Releases](https://github.com/der-manu/1Audiobook/releases) laden (Android 8.0+, 64-Bit-ARM).
Ein F-Droid-Eintrag ist geplant. Quellen und Lizenzen stehen in [NOTICE.md](NOTICE.md) und im „Über"-Bildschirm.

**KI-gestützt entwickelt („vibe coded"):** Der Großteil der Änderungen seit dem Fork entstand gemeinsam mit einem
KI-Assistenten (Claude von Anthropic) und wurde vom Maintainer auf einem echten Gerät getestet. Der Code ist offen,
bitte prüfe ihn, bevor du dich darauf verlässt. Es gibt keine Gewährleistung.
