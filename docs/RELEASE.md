# Releasing 1Audiobook

Releases are built by GitHub Actions when a version tag is pushed. The APK is signed with a key that only you
hold. **The key is never committed to the repository.**

## 1. One-time setup: signing key and GitHub secrets

A *signing key* proves that updates really come from you. Android only installs an update if it is signed with the
same key as the installed version. **If you lose the key or its passwords, nobody can update your app any more.**
The key is never committed to the repository. GitHub *secrets* are encrypted values that only your workflows can read.
After saving, a secret can be replaced but never viewed again.

### 1a. Create the key (on your computer)

You need `keytool`, which comes with a JDK (Debian/Ubuntu: `sudo apt install openjdk-17-jdk-headless`).

```bash
keytool -genkeypair -v -keystore 1audiobook-release.jks -alias release \
  -keyalg RSA -keysize 4096 -validity 10000
```

`keytool` asks for a password (twice), then for your name and organisation (you may enter anything or only your name),
and finally for the key password (press Enter to use the same one). **Write the passwords down** and **copy the file
`1audiobook-release.jks` to at least two safe places** (for example a password manager and an encrypted USB stick).
Never put it into the repository or send it by e-mail.

### 1b. Turn the key into text

```bash
base64 -w0 1audiobook-release.jks > keystore.b64
```

Open `keystore.b64` in a text editor and copy its whole content (one long line).

### 1c. Create the four secrets on GitHub

1. Open your repository on github.com and click the **Settings** tab (top row, far right).
2. In the left menu click **Secrets and variables**, then **Actions**.
3. Click the green **New repository secret** button.
4. Enter the *Name*, paste the *Secret* (value) and click **Add secret**. Repeat for all four:

| Name | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | the content of `keystore.b64` |
| `RELEASE_KEYSTORE_PASSWORD` | the keystore password |
| `RELEASE_KEY_ALIAS` | `release` (the alias from step 1a) |
| `RELEASE_KEY_PASSWORD` | the **same value as the keystore password**. With the default keystore type (PKCS12) the key password is always identical to the keystore password, whatever you typed at the prompt |

Names must match exactly, including upper case. Afterwards delete `keystore.b64` from your computer
(`shred -u keystore.b64`), it is the key in readable form.

Alternative with the GitHub command line tool (`gh auth login` first):

```bash
gh secret set RELEASE_KEYSTORE_BASE64 < keystore.b64
gh secret set RELEASE_KEYSTORE_PASSWORD
gh secret set RELEASE_KEY_ALIAS
gh secret set RELEASE_KEY_PASSWORD
```

(Without `<`, `gh` asks you to type or paste the value.)

### 1d. Check the secrets

Under *Settings → Secrets and variables → Actions* you should see all four names. Then open the **Actions** tab, choose
**Release** on the left and click **Run workflow**. This builds and signs a test APK without publishing anything. A green
run means the key and all four secrets are correct. The workflow files live in `.github/workflows/`
(`build-apk.yml` for debug builds, `release.yml` for releases).

## 2. Prepare a release

1. Raise `versionCode` (a whole number, always increasing) and set `versionName` in `gradle/libs.versions.toml`.
2. Add a section to `CHANGELOG.md`.
3. Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` and the same for `de-DE` (max. 500 characters).
4. Commit and push.

## 3. Publish

```bash
git tag v1.0.0-rc.1        # must match versionName, prefixed with "v"
git push origin v1.0.0-rc.1
```

The workflow *Release* builds `assembleRelease`, signs it, and creates a GitHub release with the APK and a SHA-256
checksum. Tags that contain `rc` become pre-releases. You can also start it manually under *Actions → Release*.

## 4. Verify the result

```bash
sha256sum -c 1Audiobook-v1.0.0-rc.1.apk.sha256
apksigner verify --print-certs 1Audiobook-v1.0.0-rc.1.apk
```

## Building a signed release locally

```bash
export RELEASE_KEYSTORE_FILE=/path/to/1audiobook-release.jks
export RELEASE_KEYSTORE_PASSWORD=...
export RELEASE_KEY_ALIAS=release
export RELEASE_KEY_PASSWORD=...
./gradlew assembleRelease
```

Without these variables the release APK is **unsigned**. That is intended: F-Droid signs apps itself.

## Troubleshooting

**`Failed to read key … Get Key failed: Given final block not properly padded`** (task `:app:packageRelease`)
The keystore could be opened, but the key password is wrong. With a PKCS12 keystore (the default of current JDKs) set
`RELEASE_KEY_PASSWORD` to exactly the same value as `RELEASE_KEYSTORE_PASSWORD`. Check the type with
`keytool -list -v -keystore 1audiobook-release.jks` (line *Keystore type*). Re-enter both secrets by pasting them in the
GitHub web form and make sure there is no space or line break at the end; then use *Re-run all jobs*.

**`keystore password was incorrect`** The secret `RELEASE_KEYSTORE_PASSWORD` is wrong, or `RELEASE_KEYSTORE_BASE64` was
not copied completely.
