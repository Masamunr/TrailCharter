# TrailCharter Project Status

## Phase
**Product Design + Foundation Development**

## Product name
**TrailCharter — AGREE**

The name is agreed for current development and branding work but is not yet FINAL.

## Repository
- Authoritative repository: **`Masamunr/TrailCharter`**
- Visibility: **public**
- Historical path: `Masamunr/altrove` (superseded by repository rename on 2026-08-28)
- GitHub remains the authoritative source of truth for project documentation and Android source.

## Current product boundary
- Geographic scope: **United Kingdom only** — England, Scotland, Wales and Northern Ireland
- Crown Dependencies and other countries: outside current scope
- Live location sharing: **not in current product/release scope**
- Core architecture: **no servers owned or administered by the TrailCharter project required for core functionality**
- Status: **AGREE**

## Android application identity
- Namespace: `com.masamunr.trailcharter`
- Application ID: `com.masamunr.trailcharter`
- Status: **AGREE**

## Android SDK baseline
- `minSdk = 28`
- `targetSdk = 36`
- `compileSdk = 36`
- Status: **AGREE**

## Android build toolchain
- Android Gradle Plugin: `8.13.2`
- Gradle: `8.13`
- Kotlin: `2.3.21`
- JDK: `17`
- Build scripts: Kotlin DSL
- Dependency/version management: Gradle version catalog (`libs.versions.toml`)
- Committed Gradle Wrapper generated and checksum-verified from Gradle 8.13
- Status: **AGREE / IMPLEMENTED**

## Compose / UI foundation
- Jetpack Compose-first UI
- Compose BOM `2026.06.00`
- Material 3 as the behaviour/accessibility foundation
- Single-activity application
- TrailCharter-owned colour, typography, shapes and component styling
- TrailCharter-controlled seasonal palettes; Android dynamic colour is not the default
- Edge-to-edge and responsive/adaptive layouts from inception
- Accessibility-first component behaviour and semantics
- No ordinary XML screen layouts
- Final primary navigation model remains **EXPLORE**
- Current foundation screen is explicitly provisional and does not lock the final in-app design language or navigation
- Status: **AGREE / FOUNDATION IMPLEMENTED**

The UI foundation does not lock TrailCharter into stock Material appearance. Material 3 supplies reliable interaction/accessibility mechanics; TrailCharter retains control over the app's overall look, layout, components, motion and visual design.

## Local persistence foundation
- Room for structured TrailCharter data — **AGREE, implementation deferred until the first structured Adventure model is defined**
- DataStore for preferences/settings — initial seam implemented
- App-managed files for GPX, attachments and later offline/map packages
- Storage access hidden behind repository/interfaces rather than used directly by UI
- KSP for generated Room/database code when Room is introduced
- Core/personal Adventure data stored locally as the authoritative copy
- No cloud/backend dependency for core functionality
- Explicit versioned migrations from the beginning once the Room database exists; no destructive production migrations
- Android Keystore-backed handling for credentials/key material if later required
- Enhanced application-level encryption beyond normal Android app sandbox/device encryption remains **EXPLORE** pending a proper threat model
- Status: **AGREE / PARTIAL FOUNDATION IMPLEMENTED**

## Privacy / network / backup foundation
- TrailCharter is **network-silent by default**
- Current foundation application does **not** declare `INTERNET` permission
- Current manifest declares no location or broad-storage permission
- When network-dependent features arrive, access is centralised behind a TrailCharter network boundary and occurs only for clear user-initiated or explicitly enabled functions
- Merely opening/using core TrailCharter functionality must not create background connections
- No advertising, analytics, telemetry, tracking SDKs or silent uploads
- Cleartext HTTP is disabled in the current foundation
- No TrailCharter cloud backup service
- Automatic Android cloud backup of personal/core data disabled/excluded in the current foundation
- Backup/export remains user-initiated and locally created
- System document picker / Storage Access Framework remains the agreed destination/restore model
- Least-privilege contextual permissions; no startup permission barrage; graceful operation when denied
- Prefer Photo Picker and Storage Access Framework over broad media/storage access
- Background location requires a separate future decision
- Provisional Privacy Status foundation exposes clear location tracking, internet and cloud-backup states
- Exact backup archive format and optional user-controlled backup encryption remain **EXPLORE**
- Status: **AGREE / FOUNDATION IMPLEMENTED**

## CI / build foundation
- GitHub Actions is the Android CI platform
- Permanent workflow: `.github/workflows/android-ci.yml`
- Runs on Android/build-file changes for pull requests and pushes to `main`; manual dispatch is available
- Runner: `ubuntu-latest`
- JDK: Temurin 17
- Android SDK/API 36 provisioned with `android-actions/setup-android@v4`
- Gradle: committed Gradle Wrapper with setup/caching/wrapper validation via `gradle/actions/setup-gradle@v6`
- Verification gate: `testDebugUnitTest`, `lintDebug`, `assembleDebug`
- Successful builds upload a debug APK artifact; failure reports/logs are uploaded where available
- Debug APK retention: 14 days; failure reports: 7 days
- No production signing secrets required for ordinary CI/debug builds
- No automatic Google Play publishing at this stage
- Detailed CI rules: `docs/architecture/CI_BUILD.md`
- Status: **AGREE / IMPLEMENTED / VERIFIED**

Latest authoritative verification:
- `main` source merge: `2099d1198875f2dad624ecae86dff2bbcb04f5fc`
- Android CI run: **#3** (`33170566092`) — **PASS**
- Unit tests: PASS
- Android lint: PASS
- Debug assembly: PASS
- Artifact: `trailcharter-debug-run-3`
- Artifact digest: `sha256:66863ec48d90cffc6dc9a68122db6059402a8babf6614f148ba2eab2fecd480b`

## Implementation
The first Android foundation is now implemented and merged to `main`.

It is an installable development foundation, not a feature-complete or production-release application. It establishes the agreed Android/toolchain/privacy seams without prematurely locking the Adventure information architecture, navigation model, mapping architecture or wider in-app visual design.

## Current state
- Project history consolidated and governance documents established.
- TrailCharter selected as the current agreed product name.
- Current product/release geography narrowed to the United Kingdom only.
- Live location sharing removed from the current product/release scope.
- Zero-owned-server architecture agreed for core TrailCharter functionality.
- Repository renamed to `Masamunr/TrailCharter`, made public after a pre-publication history/secret audit, and retained as authoritative source of truth.
- Android package/application identity agreed as `com.masamunr.trailcharter`.
- Android SDK baseline agreed as API 28 minimum and API 36 target/compile.
- Android build toolchain agreed and implemented as AGP 8.13.2 / Gradle 8.13 / Kotlin 2.3.21 / JDK 17 with Kotlin DSL and version catalog.
- Compose-first, Material 3-backed UI foundation implemented while preserving full TrailCharter visual-design control.
- DataStore settings seam implemented; Room remains deliberately deferred until the structured Adventure model exists.
- Network-silent privacy rules are reflected in the current foundation manifest/configuration: no INTERNET/location/broad-storage permissions, cloud backup excluded, and cleartext traffic disabled.
- CI/build foundation is operational and verified on both the foundation PR and post-merge `main`.
- PR #1 (`Add TrailCharter Android foundation`) merged successfully on 2026-08-28.
- Seasonal icon system remains locked as FINAL.
- Brand specification remains at `docs/brand/BRAND_IDENTITY.md`.
- Asset gap remains: the exact approved no-shield launcher-art reference binary still needs to be committed to the repository; its visual rules remain authoritative in the brand specification.

## Implementation guardrails
- Do not prematurely hard-code unresolved EXPLORE decisions.
- Keep architecture modular and replaceable where major technical choices remain open.
- Prioritise privacy-first, offline-first and local-first foundations.
- Do not introduce accounts, ads, analytics, telemetry or silent uploads.
- Do not introduce a TrailCharter-owned application backend for core functionality.
- Treat UK mapping/routing architecture as an investigation boundary until the Organic Maps/OpenStreetMap approach is properly resolved.

## Next priorities
1. Preserve the exact FINAL launcher icon reference asset in GitHub.
2. Define the Adventure information model and main UX/navigation model.
3. Continue product identity beyond the launcher icon: wordmark, typography, in-app visual design language and seasonal theme application.
4. Investigate UK map/offline architecture and package distribution within the zero-owned-server constraint.
5. Build the first useful Adventure vertical slice once the information model is agreed.

## Implementation gate
**OPEN — AGREE**

TrailCharter may continue iterative development alongside product and architecture design. Unresolved EXPLORE decisions remain open and must not be treated as settled merely because the Android foundation now exists.
