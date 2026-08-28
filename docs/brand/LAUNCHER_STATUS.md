# TrailCharter launcher status

FINAL launcher identity: the approved no-shield mountains / trees / river / sun composition shown in the supplied TrailCharter launcher reference. Android may crop or scale that approved composition only as required for platform masks; it must not be redesigned.

The launcher implementation uses a binary WebP source derived directly from the supplied approved reference rather than the failed Base64 text-transfer representation. The build validates the file structure and approved SHA-256 before generating regular, round and adaptive launcher resources.

PR #2 (`Apply TrailCharter launcher branding`) was merged into `main` as `afdb2d0f5f9e0e6b1018ecc5e82461af2797cb09` after PR Android CI run #12 passed unit tests, lint, debug assembly and APK upload. Post-merge Android CI run #13 also passed on `main` and produced artifact `trailcharter-debug-run-13` (artifact ID `9689007005`). The verified APK is versionCode 2 / versionName `0.1.1-foundation`; its SHA-256 is `a620448a85a4f7f610a50ba55dd93301abd0567112046ff4f064d4fb9c652480`.
