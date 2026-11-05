# Changelog

## 1.0.0-rc.1 (first release candidate)

1Audiobook is derived from PlayBook 4.1.1 by Goodwy (based on Voice by Paul Woitaschek). Changes since then:

### New
- Library import with an editable preview: automatic detection of books, multi-CD books, author and series
  folders; cleaned-up titles; nothing is added before you confirm.
- `.m3u` playlists decide the order when usable; broken playlists are detected and replaced by sorting.
- Playlist order dialog with live preview (group by folder, file name, tag title, date, reversed, *Magic* for CD
  numbers in file names) and sideways scrolling for long file names.
- Quick light/dark switch in the settings.
- New name, application id (`io.github.dermanu.oneaudiobook`) and app icon.
- Hidden files and macOS leftovers (`._*`, `__MACOSX`) never end up in a playlist.
- Online cover search is **off by default**. A switch in *Settings → Cover* ("Search missing covers via
  DuckDuckGo") turns it on; only then does the book menu offer *Cover from the Internet*.

### Removed
- All app store connections: Google Play Billing, in-app review, the Play publishing plugin, the RuStore SDK and
  every purchase, donation and "Pro" screen. All features are free.
- Build-time telemetry (Develocity build scans) and automatic JDK downloads.
- Dead code, unused settings and 64 unused texts (removed from all translations).

### Changed
- Only 64-bit ARM (`arm64-v8a`) is supported, because the maintained FFmpegKit fork only ships that library.
- The retired FFmpegKit dependency was replaced by a maintained fork.
- Release builds are signed from environment variables (see `docs/RELEASE.md`) instead of a committed key.
