# TrailCharter launcher status

FINAL landscape identity: the approved mountains / trees / river / sun composition. The landscape artwork itself must not be redesigned.

FINAL Android launcher treatment: the approved shield containing that landscape, centred on the TrailCharter dark-green field (`#1F3D2E`) and constrained within Android's centred 66 dp never-clipped adaptive-icon safe zone.

Physical-device testing established the accepted implementation through several rejected iterations:

- `0.1.1-foundation`: no-shield adaptive foreground appeared oversized/cropped.
- `0.1.2-foundation`: inset scale was improved, but duplicate adaptive artwork appeared around the foreground.
- `0.1.3-foundation`: duplicate artwork was removed, but the no-shield framed result remained visually unsuitable.
- `0.1.4-foundation`: the previously approved shield treatment was reintroduced, but the finished square shield image was still being treated as a full 108 dp adaptive foreground and remained too large on-device.
- `0.1.5-foundation`: the shield was constrained to Android's centred 66 dp never-clipped safe zone over a separate plain `#1F3D2E` background. This implementation was physically tested and accepted on-device on 2026-08-28.

The accepted launcher implementation was delivered through PR #6 (`Constrain launcher to Android adaptive safe zone`). PR Android CI run #21 passed unit tests, lint, debug assembly and APK upload. PR #6 was merged into `main` as `40cc682480e5f1023b445104b1bd106a950f67a2`; post-merge Android CI run #22 also passed and produced `trailcharter-debug-run-22` (artifact ID `9692766082`).

The accepted APK is versionCode 6 / versionName `0.1.5-foundation`; its SHA-256 is `22a72c5abd307b472dcf00a5dcc9dfe7146bf72ff4dcb4b60f8870ade6970512`.

The launcher-container decision is no longer EXPLORE. The shield + Android safe-zone implementation is FINAL unless explicitly reopened.
