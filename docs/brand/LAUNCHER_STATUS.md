# TrailCharter launcher status

FINAL launcher identity: the approved no-shield mountains / trees / river / sun composition shown in the supplied TrailCharter launcher reference. The artwork itself must not be redesigned.

The launcher implementation uses a binary WebP source derived directly from the supplied approved reference. The build validates the approved source SHA-256 before generating launcher resources.

Physical-device testing of version `0.1.1-foundation` showed that the first adaptive implementation was visually oversized/cropped. Version `0.1.2-foundation` corrected the scale by insetting the approved landscape, but physical-device testing then exposed a second implementation defect: the same landscape was also retained as the full-size adaptive background, so duplicate sun, mountain and tree fragments appeared around the inset foreground.

PR #4 (`Remove duplicate adaptive launcher artwork`) removes that duplicate composition. The adaptive icon now uses one unchanged copy of the approved landscape in the inset foreground with the approved pale neutral brand background (`#F2ECE2`) behind it. No artwork is redrawn or reinterpreted.

PR CI run #16 passed unit tests, lint, debug assembly and APK upload. PR #4 was merged into `main` as `bae7e0e6b70f2136b6085be76c7024f92af3b783`; post-merge Android CI run #17 also passed and produced artifact `trailcharter-debug-run-17` (artifact ID `9690611584`).

The resulting APK is versionCode 4 / versionName `0.1.3-foundation`; its SHA-256 is `5ffb65a8436a6445d4f216ee4bf72b04c75e2fdb204463f1f1f2152d21d000db`. Physical-device confirmation remains required before this adaptive-icon framing is treated as accepted.
