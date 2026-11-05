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

## Still open

1. **Cover search (decided: keep, off by default).** It uses DuckDuckGo, a non-free network service, and the user has to
   switch it on in the settings. F-Droid may still label the app with the anti-feature `NonFreeNet`; say in the merge
   request that the feature is opt-in. The alternative would be to remove it and the `INTERNET` permission.
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

## Draft recipe (`metadata/io.github.dermanu.oneaudiobook.yml` in fdroiddata)

```yaml
Categories:
  - Multimedia
License: GPL-3.0-only
SourceCode: https://github.com/der-manu/1Audiobook
IssueTracker: https://github.com/der-manu/1Audiobook/issues
Changelog: https://github.com/der-manu/1Audiobook/blob/HEAD/CHANGELOG.md

AutoName: 1Audiobook

RepoType: git
Repo: https://github.com/der-manu/1Audiobook.git

Builds:
  - versionName: 1.0.0-rc.1
    versionCode: 1
    commit: v1.0.0-rc.1
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0-rc.1
CurrentVersionCode: 1
```

Add `AntiFeatures: NonFreeNet` if the cover search stays. This is a draft: the F-Droid maintainers review the final
recipe in the merge request.

## Submitting

1. Fork <https://gitlab.com/fdroid/fdroiddata>, add the recipe above, and open a merge request.
2. Describe the app and its added value in the merge request, mention the `arm64-v8a` limitation and the optional
   network use.
3. Answer review comments. Store texts and images come from `fastlane/metadata/android/` in this repository.
