# Privacy policy / Datenschutz

1Audiobook is free, open-source software. This policy describes the builds published from this repository.
*Diese Erklärung gilt für die aus diesem Repository veröffentlichten Builds.*

## What the app does not do

* It does **not** collect, store on a server, or transmit personal data.
* It contains **no** analytics, crash reporting, advertising, tracking or account system.
* It contains **no** Google Play Services, Google Play Billing, in-app purchases or any app store connection.

## What stays on your device

Your library, listening positions, bookmarks, settings, chosen book titles and the playlist order you pick for each
book are stored only on your device. The app reads the folders you explicitly grant access to through Android's
folder picker. It does not change your audio files. There are two exceptions, and you control both:

* the *Delete Book* action: if you choose it and explicitly confirm *Delete my files*, the files of that book are
  deleted from your device;
* the setting *Also save chosen covers in the book folder* (**off by default**): only if you switch it on, the app
  writes one image file, `cover.png`, into the folder of a book when you choose a cover for it. It replaces only its own
  `cover.png` and never touches other files.

## Network use

The app works fully offline. The only feature that uses the internet is the **cover search**, which is **switched off by
default**. You can switch it on under *Settings → Cover → Search missing covers via DuckDuckGo*. Only then does the
book menu offer *Cover from the Internet*, and if you use it:

* the search term you type (usually the book title) is sent to the image search of
  [DuckDuckGo](https://duckduckgo.com/privacy);
* the image you pick is downloaded from the server that hosts it, which can see your IP address.

While the switch is off, the app makes no network requests. Tapping a link in the *About* screen opens
it in your browser.

## Permissions

| Permission | Why |
|---|---|
| Foreground service (media playback), wake lock | keep audio playing with the screen off |
| Ignore battery optimizations (optional request) | avoid playback being stopped by the system |
| Internet, network state | cover search (only if you switch it on) |

## Contact

Questions about this policy: open an [issue](https://github.com/der-manu/1Audiobook/issues).
