# TrailCharter Specification

## AGREE
- Product name: **TrailCharter** (AGREE, not yet FINAL)
- Android namespace: `com.masamunr.trailcharter`
- Android application ID: `com.masamunr.trailcharter`
- Android SDK baseline: `minSdk = 28`, `targetSdk = 36`, `compileSdk = 36`
- Android build toolchain: Android Gradle Plugin `8.13.2`, Gradle `8.13`, Kotlin `2.3.21`, JDK `17`, Kotlin DSL build scripts, Gradle version catalog (`libs.versions.toml`)
- Jetpack Compose-first UI using Compose BOM `2026.06.00`
- Material 3 as the interaction/accessibility foundation, with TrailCharter-owned visual styling
- Single-activity architecture
- Edge-to-edge layouts from inception
- Adaptive/responsive layouts from inception
- Accessibility-first component design
- No ordinary XML screen layouts for app UI
- TrailCharter controls its seasonal palettes; Android dynamic colour is not enabled by default
- Final navigation structure remains EXPLORE and must not be prematurely locked by the Android foundation
- Local-first persistence: Room for structured application data; DataStore for preferences/settings; app-managed files for GPX, attachments and later offline/map packages
- Storage is hidden behind repository/interfaces rather than accessed directly by UI
- Use KSP for generated Room/database code
- Core/personal Adventure data is local and authoritative; no cloud/backend dependency for core functionality
- Explicit versioned database migrations are required from the beginning; destructive production migrations are not permitted
- Credentials or cryptographic key material, if later required, use Android Keystore-backed handling
- **Offline by default:** the initial app does not declare `INTERNET` permission. If network-dependent capabilities are added later, ordinary app operation remains offline unless a user action or explicitly enabled feature requires network access.
- Network access is centralised behind a defined TrailCharter network boundary; features must not create ad-hoc background connections.
- No advertising, analytics, telemetry, tracking SDKs or silent uploads.
- When networking exists, cleartext HTTP is disabled for ordinary app networking and secure transport is required.
- No TrailCharter cloud backup service and no cloud dependency for backup/restore.
- Automatic Android cloud backup of TrailCharter personal/core data is disabled/excluded as far as the platform permits.
- Backups are created locally and explicitly by the user.
- Android Storage Access Framework/system document picker is used for choosing backup/export destinations and restore/import sources. A user may therefore place a locally created backup into a personal provider they already use, such as Proton Drive or OneDrive, where that provider is exposed through Android. TrailCharter does not directly authenticate to or require those providers for backup.
- Permission model is least-privilege and contextual: request only when the user invokes a feature requiring it; no startup permission barrage; denied permissions must degrade gracefully.
- Prefer Android Photo Picker and Storage Access Framework over broad media/storage permissions.
- Background location is not part of the foundation permission set and requires a separate future decision.
- Privacy Status includes an unambiguous location-tracking state such as `On` / `Off`; richer privacy-status information may be added alongside later capabilities.
- Android-first
- Privacy-first
- Offline-first wherever practical
- Open-source intended
- No account required for core functionality
- No advertising
- No analytics or telemetry by default
- No silent uploads
- Location remains on-device unless explicitly shared
- Network actions must be transparent
- OpenStreetMap foundation
- TrailCharter is an adventure-planning application, not merely navigation
- Live location sharing is agreed in principle: opt-in, recipient-controlled, duration-controlled, visibly active, immediately stoppable, honest about connectivity loss
- Seasonal launcher icon system in `docs/brand/BRAND_IDENTITY.md` is FINAL

## Transparency by Design
Whenever TrailCharter accesses GPS, downloads data, stores information, connects to the internet or shares data, users should be able to understand what is happening, why, where data is going and how to stop it.

## EXPLORE
- Exact adventure information architecture
- Primary UX: timeline/map/cards/hybrid
- Optional Android system/dynamic colour mode
- Enhanced application-level encryption/threat model beyond normal Android app sandbox/device encryption
- Exact TrailCharter backup archive format and optional user-controlled backup encryption
- Organic Maps reuse/integration/fork strategy
- Offline map/routing architecture
- Adventure Download packaging
- Live-location technical architecture and E2E encryption
- Safety tooling
- Equipment/pack-weight model
- Food/water model
- Journaling model
- Exact V1 scope
