# TrailCharter Roadmap

## Phase 0 — Product Design
- Consolidate project history
- Establish constitution/specification
- Lock product identity
- Define Adventure information model
- Define UX model
- Investigate mapping/offline architecture
- Investigate Organic Maps relationship/licensing
- Define privacy architecture

## Phase 1 — Android Foundation — OPEN
Implementation may now proceed incrementally while unresolved product and architecture questions remain EXPLORE.

Foundation decisions completed:
- package/application identity: `com.masamunr.trailcharter`
- SDK baseline: `minSdk 28`, `targetSdk 36`, `compileSdk 36`
- build toolchain: AGP `8.13.2`, Gradle `8.13`, Kotlin `2.3.21`, JDK `17`, Kotlin DSL, Gradle version catalog
- Compose/UI foundation: Compose-first, Compose BOM `2026.06.00`, Material 3 behaviour/accessibility foundation, single activity, TrailCharter-owned design system, edge-to-edge/adaptive/accessibility-first UI; final navigation remains EXPLORE
- local-first persistence: Room for structured data, DataStore for preferences, app-managed files for GPX/attachments/offline packages, repository boundaries, KSP, explicit migrations, no destructive production migrations, local data authoritative
- privacy/network: offline by default, initial app without `INTERNET` permission, centralised future networking, no ads/analytics/telemetry/tracking, no silent uploads, secure transport when networking is added
- backup/export: no TrailCharter cloud backup, automatic Android cloud backup disabled/excluded as far as practical, locally created user-initiated backups with Storage Access Framework destinations including user-controlled providers such as Proton Drive/OneDrive where available through Android
- permissions/privacy status: contextual least privilege, Photo Picker/Storage Access Framework preferred, background location deferred, clear location-tracking On/Off status

Remaining initial foundation scope:
- CI/build pipeline
- branding/theme implementation
- exact backup archive format and optional user-controlled backup encryption
- replaceable architectural seams for unresolved map/routing choices

Do not prematurely implement complex mapping, routing or live-sharing architecture before the relevant EXPLORE decisions mature.

## Later / progressive capability phases
- map foundation
- offline regions
- GPX
- itinerary/stages
- privacy status expansion
- equipment/weight
- food/water
- safety
- live sharing
- journal
