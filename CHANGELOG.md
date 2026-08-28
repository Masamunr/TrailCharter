# Changelog

## Unreleased
### Design
- Consolidated Altrove + Adventure history.
- Established constitution and vision.
- Classified current decisions.
- Locked seasonal launcher icon system as FINAL.
- 2026-08-28: Selected **TrailCharter** as the current product name — **AGREE**, not yet FINAL — following deeper collision/clearance checks and contextual brand testing.
- Altrove as a product name is rejected/superseded because of a directly adjacent active travel-planning product using that name.

### Governance
- 2026-08-27: Reopened the Android implementation gate as an AGREE decision.
- Controlled Android foundation development may now proceed alongside continued product and architecture design.
- Unresolved EXPLORE decisions must not be prematurely hard-coded into the product or architecture.

### Repository
- 2026-08-27: GitHub access restored in a new conversation and the portable project baseline was reconciled into `Masamunr/altrove`.
- Added the constitution, vision, specification, roadmap, decisions, project status and brand identity documentation to the repository.
- The historical repository path remains `Masamunr/altrove` for continuity while TrailCharter remains AGREE rather than FINAL.
- The exact approved no-shield launcher-art reference binary still needs to be committed; the FINAL visual rules are preserved in `docs/brand/BRAND_IDENTITY.md`.

### Android foundation
- 2026-08-28: Agreed Android namespace and application ID: `com.masamunr.trailcharter`.
- 2026-08-28: Agreed SDK baseline: `minSdk = 28`, `targetSdk = 36`, `compileSdk = 36`.
- 2026-08-28: Agreed build toolchain: Android Gradle Plugin `8.13.2`, Gradle `8.13`, Kotlin `2.3.21`, JDK `17`, Kotlin DSL and Gradle version catalog.
- 2026-08-28: Agreed Compose/UI foundation: Compose-first, Compose BOM `2026.06.00`, Material 3 behaviour/accessibility foundation, single activity, TrailCharter-owned design system, TrailCharter-controlled seasonal palettes, no default Android dynamic colour, edge-to-edge/adaptive/accessibility-first layouts, and no ordinary XML screen layouts. Final primary navigation remains EXPLORE.
- 2026-08-28: Agreed local-first persistence architecture: Room for structured data, DataStore for settings, app-managed files for GPX/attachments/later offline packages, repository boundaries, KSP, explicit versioned migrations with no destructive production migrations, locally authoritative core data, and Keystore-backed handling for future secrets/key material.
- 2026-08-28: Agreed offline/privacy foundation: TrailCharter is offline by default; the initial app does not declare `INTERNET` permission; future networking is centralised and only used for explicit/user-enabled features; no advertising, analytics, telemetry, tracking SDKs or silent uploads.
- 2026-08-28: Agreed local-only backup/export model: no TrailCharter cloud backup service, Android automatic cloud backup disabled/excluded as far as practical, backups created locally on user request, and destinations selected through Android Storage Access Framework. Users may choose a personal provider such as Proton Drive or OneDrive where exposed by Android, without direct TrailCharter cloud integration.
- 2026-08-28: Agreed least-privilege contextual permissions with no startup permission barrage, preference for Photo Picker/Storage Access Framework over broad media/storage access, background location deferred, and a clear Privacy Status location-tracking On/Off state.
- Android implementation is authorised but no production application code has been built yet.
