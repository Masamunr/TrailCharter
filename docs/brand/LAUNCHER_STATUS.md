# TrailCharter launcher status

FINAL launcher identity: the approved no-shield mountains / trees / river / sun composition shown in the supplied TrailCharter launcher reference. The artwork itself must not be redesigned.

The launcher implementation uses a binary WebP source derived directly from the supplied approved reference. The build validates the approved source SHA-256 before generating launcher resources.

A physical-device check of version `0.1.1-foundation` identified that Android's adaptive-icon mask was visually enlarging/cropping the composition because the full artwork had been placed across the complete 108 dp adaptive layer. Android normally exposes the inner 72 dp while reserving 18 dp on each side for masking and effects.

PR #3 (`Fix adaptive launcher icon framing`) corrects that implementation without changing the approved artwork: the original scene remains full-bleed behind the adaptive mask and the same unchanged scene is inset 18 dp on each side for the visible foreground viewport. PR CI run #14 passed unit tests, lint, debug assembly and APK upload. PR #3 was merged into `main` as `f3e546b2d1d0dfd2bf58098a17d870cf4558e9bc`; post-merge Android CI run #15 also passed and produced `trailcharter-debug-run-15`.

The resulting APK is versionCode 3 / versionName `0.1.2-foundation`; its SHA-256 is `c17724ed4a3e8e1d0457ca1acade4074dd49c0b619902ae832cf08c982a3246b`. Physical-device confirmation of the corrected framing is still pending and should be treated as the final acceptance check for this launcher-size correction.
