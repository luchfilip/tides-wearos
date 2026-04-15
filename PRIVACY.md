# Privacy Policy

_Last updated: 2026-04-15_

Tides is an unofficial Tidal music player for Wear OS. This policy
explains what data the app handles, where it goes, and what it does not
do.

## TL;DR

- Tides does not operate any server of its own.
- Tides does not collect, transmit, or share any personal data with the
  developer or any third party other than Tidal.
- No analytics. No crash reporting. No advertising. No tracking.
- Everything the app knows about you stays on your watch.

## What data Tides handles

**Tidal authentication tokens.** When you sign in, Tides uses the
official Tidal SDK to exchange a device code for an access token and a
refresh token. These tokens are stored locally on your device by the
Tidal SDK. They are used only to make API calls to Tidal on your
behalf. They are never sent anywhere else.

**Playback state.** The currently playing track, queue, and playback
position are held in memory on your device while the app is running.
None of this is transmitted off the device.

**App preferences.** Any settings you change (for example, quality
preferences) are stored locally on your watch in standard Android
preferences. None of this is transmitted off the device.

## Network requests

Tides makes network requests only to Tidal's own servers
(`api.tidal.com`, `auth.tidal.com`, and related endpoints) for the
purposes of authentication, fetching your library, and streaming audio.
All traffic uses HTTPS.

These requests are subject to Tidal's own privacy policy, available at
<https://tidal.com/privacy>. Tides has no control over what Tidal
logs or retains.

## What Tides does not do

- Tides does **not** send any data to any server operated by the
  developer. There is no such server.
- Tides does **not** include any analytics SDK (no Firebase, no
  Crashlytics, no Google Analytics, no Sentry, no third-party
  telemetry).
- Tides does **not** show advertisements.
- Tides does **not** track you across apps or websites.
- Tides does **not** access your contacts, photos, location, or any
  sensor data. The app requests only the permissions listed below.

## Permissions the app requests

- `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` — used to
  talk to Tidal's servers and detect whether you are online.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`,
  `FOREGROUND_SERVICE_DATA_SYNC`, `WAKE_LOCK` — used to keep music
  playing in the background and to keep the watch awake during
  playback.
- `POST_NOTIFICATIONS` — used to show the Now Playing notification
  so you can control playback from the watch's system UI.
- `BLUETOOTH_CONNECT` — used to route audio to your Bluetooth
  headphones.

## Data deletion

Because Tides stores everything locally on your device, the way to
delete your data is to uninstall the app. Uninstalling removes all
local tokens, preferences, and playback state from your watch.

Your Tidal account is managed entirely by Tidal. To delete it, see
Tidal's own account deletion instructions.

## Children

Tides is not directed at children under 13. The app does not
knowingly handle data from children under 13.

## Changes to this policy

If this policy changes, the new version will be committed to this
repository and the "Last updated" date above will change. Because the
repository is public, the full history of this file is visible in the
Git log at
<https://github.com/luchfilip/Tides-WearOS/commits/main/PRIVACY.md>.

## Contact

- Issues: <https://github.com/luchfilip/Tides-WearOS/issues>
- Email: fldevconsole@gmail.com

## Disclaimer

Tides is an independent, community-built project. It is not affiliated
with, endorsed by, or connected to TIDAL or Block, Inc. "Tidal" is a
trademark of Block, Inc.
