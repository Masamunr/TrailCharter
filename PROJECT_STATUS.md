# TrailCharter Project Status

## Phase
**Product Design + Foundation Development**

## Product name
**TrailCharter — AGREE**

The name is agreed for current development and branding work but is not yet FINAL. The existing repository path `Masamunr/altrove` is retained for continuity until a later deliberate rename decision.

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
- Status: **AGREE**

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
- Status: **AGREE**

The UI foundation does not lock TrailCharter into stock Material appearance. Material 3 supplies reliable interaction/accessibility mechanics; TrailCharter retains control over the app's overall look, layout, components, motion and visual design.

## Local persistence foundation
- Room for structured TrailCharter data
- DataStore for preferences/settings
- App-managed files for GPX, attachments and later offline/map packages
- Storage access hidden behind repository/interfaces rather than used directly by UI
- KSP for generated Room/database code
- Core/personal Adventure data stored locally as the authoritative copy
- No cloud/backend dependency for core functionality
- Explicit versioned migrations from the beginning; no destructive production migrations
- Android Keystore-backed handling for credentials/key material if later required
- Enhanced application-level encryption beyond normal Android app sandbox/device encryption remains **EXPLORE** pending a proper threat model
- Status: **AGREE**

## Privacy / network / backup foundation
- TrailCharter is **network-silent by default**
- Initial application does not declare `INTERNET` permission
- When network-dependent features arrive, access is centralised behind a TrailCharter network boundary and occurs only for clear user-initiated or explicitly enabled functions
- Merely opening/using core TrailCharter functionality must not create background connections
- No advertising, analytics, telemetry, tracking SDKs or silent uploads
- Ordinary future networking uses secure transport with cleartext HTTP disabled
- No TrailCharter cloud backup service
- Automatic Android cloud backup of personal/core data disabled/excluded as far as the platform permits
- Backup/export is user-initiated and created locally
- System document picker / Storage Access Framework used for destinations and restore sources; users may choose their own personal provider such as Proton Drive or OneDrive if it is available to Android, without TrailCharter directly integrating/authenticating to that service
- Least-privilege contextual permissions; no startup permission barrage; graceful operation when denied
- Prefer Photo Picker and Storage Access Framework over broad media/storage access
- Background location requires a separate future decision
- Privacy Status will show a clear location-tracking `On` / `Off` state
- Exact backup archive format and optional user-controlled backup encryption remain **EXPLORE**
- Status: **AGREE**

## Implementation
Android implementation is authorised as a controlled foundation phase.

No production Android application has been implemented yet.

## Current state
- Project history consolidated.
- Constitution established.
- Vision established.
- Decisions classified into EXPLORE / AGREE / FINAL.
- TrailCharter selected as the current agreed product name following deeper collision/clearance checks.
- Current product/release geography narrowed to the United Kingdom only.
- Live location sharing removed from the current product/release scope.
- Zero-owned-server architecture agreed for core TrailCharter functionality.
- Android package/application identity agreed as `com.masamunr.trailcharter`.
- Android SDK baseline agreed as API 28 minimum and API 36 target/compile.
- Android build toolchain agreed as AGP 8.13.2 / Gradle 8.13 / Kotlin 2.3.21 / JDK 17 with Kotlin DSL and version catalog.
- Compose-first, Material 3-backed UI foundation agreed while preserving full TrailCharter visual-design control.
- Local-first persistence architecture agreed using Room, DataStore and app-managed files behind repository boundaries.
- Network-silent privacy rules, local-only user-controlled backup/export, contextual permissions and location-tracking privacy status are agreed.
- Altrove as a product name is rejected/superseded because of a directly adjacent active travel-planning product using that name.
- Seasonal icon system locked as FINAL.
- GitHub repository exists at `Masamunr/altrove`.
- GitHub connector access was successfully restored and write-tested on 2026-08-27.
- The portable baseline documentation has been reconciled into the repository.
- GitHub is the authoritative source of truth for project documentation.
- Brand specification is preserved at `docs/brand/BRAND_IDENTITY.md`.
- Asset gap remains: the exact approved no-shield launcher-art reference binary still needs to be committed to the repository; its visual rules are preserved in the brand specification.
- On 2026-08-27 the previous implementation gate was reopened: controlled Android foundation development may proceed while product and architecture design continue.

## Implementation guardrails
- Do not prematurely hard-code unresolved EXPLORE decisions.
- Keep early architecture modular and replaceable where major technical choices remain open.
- Prioritise privacy-first, offline-first and local-first foundations.
- Do not introduce accounts, ads, analytics, telemetry or silent uploads.
- Do not introduce a TrailCharter-owned application backend for core functionality.
- Treat UK mapping/routing architecture as an investigation boundary until the Organic Maps/OpenStreetMap approach is properly resolved.

## Next priorities
1. Define CI/build pipeline.
2. Preserve the exact FINAL launcher icon reference asset in GitHub.
3. Continue product identity beyond the launcher icon: wordmark, typography, in-app visual design language and seasonal theme application.
4. Define the Adventure information model and main UX/navigation model.
5. Investigate UK map/offline architecture and package distribution within the zero-owned-server constraint.

## Implementation gate
**OPEN — AGREE**

TrailCharter may enter iterative foundation development alongside continued product and architecture design. Unresolved EXPLORE decisions remain open and must not be treated as settled merely because implementation has begun.
