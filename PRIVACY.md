# Privacy policy

PlayBookAdvance is free, open-source software. This policy describes the builds published from this repository
(the `libre` variant).

## What the app does not do

* It does **not** collect, store on a server, or transmit personal data.
* It contains **no** analytics, crash reporting, advertising, tracking or account system.
* It contains **no** Google Play Services, Google Play Billing or in-app purchases.

## What stays on your device

Your library, listening positions, bookmarks, settings and the playlist order you choose for each book are stored
only on your device. The app reads the folders you explicitly grant access to through Android's folder picker.

## Network use

The only feature that uses the internet is the **optional cover search**. If you use it:

* the search term you type (usually the book title) is sent to the image search of
  [DuckDuckGo](https://duckduckgo.com/privacy);
* the image you pick is downloaded from the server that hosts it, which can see your IP address.

If you never use the cover search, the app makes no network requests. Tapping a link in the *About* screen opens
it in your browser.

## Permissions

| Permission | Why |
|---|---|
| Foreground service (media playback), wake lock | keep audio playing with the screen off |
| Ignore battery optimizations (optional request) | avoid playback being stopped by the system |
| Internet, network state | optional cover search |

## Contact

Questions about this policy: open an [issue](https://github.com/der-manu/PlayBookAdvance/issues).
