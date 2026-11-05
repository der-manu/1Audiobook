# F-Droid: status and checklist

Goal: list 1Audiobook in the main F-Droid repository. This file records what the
[F-Droid Inclusion Policy](https://f-droid.org/docs/Inclusion_Policy/) asks for and where the project stands.

## Policy check

| Requirement | Status |
|---|---|
| Free software license | GPL-3.0 (`LICENSE.md`) |
| Fork needs its own Android id, name, icon and texts | `io.github.dermanu.oneaudiobook`, name *1Audiobook*, own icon, own About texts |
| No proprietary / tracking libraries (Play Services, Firebase, ads) | none; Google Play Billing, in-app review and the Play publishing plugin were removed |
| Build from source with a free toolchain | Gradle build, JDK 17, no proprietary tools |
| Prebuilt binaries only from trusted Maven repositories, and freely licensed | only Maven Central and Google Maven; FFmpegKit fork is LGPL-3.0 |
| No telemetry or downloads during the build | Develocity build scans and the Foojay JDK downloader were removed |
| Donation links must be verified | none present |
| Unique value compared with apps already in the repository | library import with preview, `.m3u` support, "Magic" sort (to be stated in the merge request) |
| Maintained | maintainer commits to updates |
| Actively developed for at least 3 months (Quality point 6) | **not yet met**: the project started in November 2026 and has no users besides the author yet. Options: wait, or open the merge request as a *Draft* to let the build checks run (see below) |

## Still open

1. **Cover search (decided: keep, off by default).** It uses DuckDuckGo, a non-free network service, and the user has to
   switch it on in the settings. The recipe therefore declares the anti-feature `NonFreeNet` with an explanation that the
   feature is opt-in. The alternative would be to remove it and the `INTERNET` permission.
2. **Name.** The upstream project is called PlayBook; this app is named 1Audiobook, so there is no name clash.
3. **Screenshots** for `fastlane/metadata/android/*/images/phoneScreenshots/` (use neutral, non-personal content).
4. **A release candidate that has been installed and tested** on a real device (see `docs/RELEASE.md`).
5. **64-bit ARM only.** State this in the merge request: the FFmpegKit fork only provides `arm64-v8a`.

## Local test with fdroidserver

```bash
fdroid readmeta
fdroid lint io.github.dermanu.oneaudiobook
fdroid build -v -l io.github.dermanu.oneaudiobook
```

## Recipe (`metadata/io.github.dermanu.oneaudiobook.yml` in fdroiddata)

Checked with `fdroid readmeta` and `fdroid lint` (fdroidserver 2.4.5) without findings. The lists of valid categories and
anti-features come from the `config/` folder of fdroiddata, so the final check of the names `Multimedia` and
`NonFreeNet` happens in the merge request.

```yaml
Categories:
  - Multimedia
License: GPL-3.0-only
AuthorName: der-manu
AuthorWebSite: https://github.com/der-manu
WebSite: https://github.com/der-manu/1Audiobook
SourceCode: https://github.com/der-manu/1Audiobook
IssueTracker: https://github.com/der-manu/1Audiobook/issues
Changelog: https://github.com/der-manu/1Audiobook/blob/HEAD/CHANGELOG.md

AutoName: 1Audiobook

RepoType: git
Repo: https://github.com/der-manu/1Audiobook.git

AntiFeatures:
  NonFreeNet:
    en-US: The optional cover search sends the book title to DuckDuckGo. It is switched off by default.
    de-DE: Die optionale Cover-Suche sendet den Buchtitel an DuckDuckGo. Sie ist standardmäßig ausgeschaltet.

Builds:
  - versionName: 1.0.0-rc.1
    versionCode: 1
    commit: REPLACE_WITH_FULL_40_CHAR_HASH
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0-rc.1
CurrentVersionCode: 1
```

`commit` must be the **full 40-character hash**, not the tag name. After the tag exists on GitHub, get the hash with

```bash
git ls-remote https://github.com/der-manu/1Audiobook.git 'refs/tags/v1.0.0-rc.1*'
```

If two lines appear, use the one that ends in `^{}` (that is the commit the annotated tag points to). The store texts and
screenshots are read from that commit, so add screenshots **before** tagging.

Reproducible builds are **not** enabled: the release build has not been verified bit-for-bit yet, and the choice cannot be
changed later (F-Droid then signs with its own key). GitHub and F-Droid APKs therefore have different signatures and
cannot update each other.

## Submitting (step by step)

The recipe file does **not** go into this repository. It goes into *your fork of the F-Droid data repository*
(`fdroiddata`) on GitLab, where F-Droid keeps one small file per app.

### Before you start

- The GitHub repository is public, **Issues are enabled**, and the default branch is `master`.
- The *Release* workflow has run and the tag `v1.0.0-rc.1` exists with a published release (F-Droid builds that tag).
- `fastlane/metadata/android/` is complete (title, descriptions, changelog `1.txt`, icon). Screenshots are optional but
  recommended; use neutral, non-personal content.
- `fdroid lint` reports nothing (see above).

### Steps

1. Create an account on <https://gitlab.com>.
2. Open <https://gitlab.com/fdroid/fdroiddata> and click **Fork**. Keep the name `fdroiddata` and make the fork public.
3. Clone your fork (a shallow clone is enough; the repository is large):
   ```bash
   git clone --depth 1 https://gitlab.com/<your-gitlab-name>/fdroiddata.git
   cd fdroiddata
   git checkout -b io.github.dermanu.oneaudiobook
   ```
4. Copy the recipe into the `metadata` folder of the clone:
   ```bash
   cp ~/Downloads/io.github.dermanu.oneaudiobook.yml metadata/
   ```
5. Optional local check (needs `pip install fdroidserver`):
   ```bash
   fdroid readmeta
   fdroid lint io.github.dermanu.oneaudiobook
   ```
6. Commit and push:
   ```bash
   git add metadata/io.github.dermanu.oneaudiobook.yml
   git commit -m "New App: 1Audiobook"
   git push -u origin io.github.dermanu.oneaudiobook
   ```
7. GitLab prints a link to create a **merge request**. Open it. The target is `fdroid/fdroiddata`, branch `master`.
   If GitLab offers a template, use it and tick the items honestly.
8. In the description, say briefly what the app does and what it adds over apps already in F-Droid (library import with
   an editable preview, `.m3u` support, "Magic" sort). Mention that only `arm64-v8a` is supported and that the optional
   cover search (off by default) is declared as `NonFreeNet`.
9. Watch the checks on the merge request and fix what they report by pushing to the same branch. Answer the reviewers'
   questions; they may ask for small changes to the recipe.
10. After the merge, F-Droid builds the app and publishes it. That can take several days.

New versions need no further merge request: with `UpdateCheckMode: Tags` F-Droid notices new `v*` tags on its own, as
long as `versionName` and `versionCode` in `gradle/libs.versions.toml` match the tag.
