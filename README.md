# PlayBookAdvance

A free, open-source audiobook player for Android 8.0+ (API 26), licensed under **GPL-3.0**.

PlayBookAdvance is a fork of [PlayBook](https://github.com/Goodwy/PlayBook) by Goodwy, which is based on
[Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek. The goal of this fork is to make
folder-based audiobook libraries (many folders, CD1/CD2 sub-folders, loosely named files) easy to load in
the right order.

> **Status:** early development. Expect rough edges. Please report problems via
> [Issues](https://github.com/der-manu/PlayBookAdvance/issues).

## What is different from PlayBook

- **Playlist order dialog with live preview** (player screen, `⋯` menu → *Playlist order…*):
  group by folder (folder 1 = 1…N, then folder 2 = 1…N) or sort all files together, by file name, tag title or
  date, optionally reversed. The preview updates immediately; the choice is remembered per book and survives
  rescans.
- **All features are free.** There is no paywall, no donation screen and no in-app purchase.
- **No Google Play Billing, no RuStore SDK.** Both were removed.
- Uses the maintained FFmpegKit fork, because the original FFmpegKit binaries were removed from Maven Central.

Planned: automatic recognition of a large library (authors, series, multi-CD books) with an editable preview
before anything is added.

## Install

Debug builds are produced by GitHub Actions: open the latest successful run under **Actions** and download the
`PlayBookAdvance-debug-apk` artifact. You may need to allow installing from your file manager.

## Build

```bash
./gradlew assembleGithubLibreDebug
```

Requires JDK 21 and the Android SDK. Only the `libre` flavor is supported by this fork.

## Credits and license

PlayBookAdvance is licensed under the [GNU General Public License v3.0](LICENSE). The original copyright
notices of PlayBook and Voice are kept. See [NOTICE.md](NOTICE.md) for the full list of projects this app builds
on, and the *About* screen in the app.

The folder-based playlist idea was inspired by the Mort Audiobook Player by Mirko Schenk. No Mort code was used.

---

## Auf Deutsch

PlayBookAdvance ist ein freier Hörbuchplayer für Android (GPL-3.0), ein Fork von PlayBook (Goodwy), das auf Voice
(Paul Woitaschek) basiert. Der Schwerpunkt liegt auf großen Ordner-Bibliotheken: Hörbücher mit Unterordnern wie
CD1/CD2 und unsauber benannten Dateien sollen in der richtigen Reihenfolge geladen werden, mit Vorschau.
Alle Funktionen sind frei, es gibt keine Spendenbildschirme und keine In-App-Käufe.
