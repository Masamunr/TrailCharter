# TrailCharter Changelog

## Unreleased
### Design
- Consolidated the earlier Altrove + Adventure project history into TrailCharter.
- Established constitution and vision.
- Classified current decisions.
- Locked seasonal launcher icon system as FINAL.
- 2026-08-28: Selected **TrailCharter** as the current product name — **AGREE**, not yet FINAL — following deeper collision/clearance checks and contextual brand testing.
- Altrove as a product name is rejected/superseded because of a directly adjacent active travel-planning product using that name.
- 2026-08-28: Narrowed current TrailCharter product/release geography to the **United Kingdom only**: England, Scotland, Wales and Northern Ireland. Crown Dependencies and other countries are deferred beyond the current scope.
- 2026-08-28: Removed live location sharing from the current product/release scope. Location tracking remains local to the device; live sharing may only be reconsidered in a later scope review.

### Governance
- 2026-08-27: Reopened the Android implementation gate as an AGREE decision.
- Controlled Android foundation development may now proceed alongside continued product and architecture design.
- Unresolved EXPLORE decisions must not be prematurely hard-coded into the product or architecture.
- 2026-08-28: Agreed a zero-owned-server architecture for core TrailCharter functionality. Core data/computation remain device-resident; any genuinely required external services must be isolated and replaceable rather than forming a TrailCharter application backend.

### Repository
- 2026-08-27: GitHub access restored and the portable project baseline was reconciled into the repository.
- Added the constitution, vision, specification, roadmap, decisions, project status and brand identity documentation.
- The exact approved no-shield launcher-art reference binary still needs to be committed; the FINAL visual rules are preserved in `docs/brand/BRAND_IDENTITY.md`.
- 2026-08-28: Added `.github/workflows/android-ci.yml` and `docs/architecture/CI_BUILD.md`.
- 2026-08-28: Completed a pre-publication repository/history audit; no committed credentials, keys, signing files or other secret material were identified. `.gitignore` was hardened for common environment/signing/private-key files.
- 2026-08-28: Renamed the repository from `Masamunr/altrove` to **`Masamunr/TrailCharter`** and changed visibility to **public**, matching the open-source direction and allowing standard GitHub-hosted Actions without the private-repository minutes constraint.

### Android foundation
- 2026-08-28: Agreed Android namespace and application ID: `com.masamunr.trailcharter`.
- 2026-08-28: Agreed SDK baseline: `minSdk = 28`, `targetSdk = 36`, `compileSdk = 36`.
- 2026-08-28: Agreed build toolchain: Android Gradle Plugin `8.13.2`, Gradle `8.13`, Kotlin `2.3.21`, JDK `17`, Kotlin DSL and Gradle version catalog.
- 2026-08-28: Agreed Compose/UI foundation: Compose-first, Compose BOM `2026.06.00`, Material 3 behaviour/accessibility foundation, single activity, TrailCharter-owned design system, TrailCharter-controlled seasonal palettes, no default Android dynamic colour, edge-to-edge/adaptive/accessibility-first layouts, and no ordinary XML screen layouts. Final primary navigation remains EXPLORE.
- 2026-08-28: Agreed local-first persistence architecture: Room for structured data, DataStore for settings, app-managed files for GPX/attachments/later offline packages, repository boundaries, KSP, explicit versioned migrations with no destructive production migrations, locally authoritative core data, and Keystore-backed handling for future secrets/key material.
- 2026-08-28: Refined the network rule to **network-silent by default**: the initial app does not declare `INTERNET` permission; future networking is centralised and only used for clear user-initiated/explicitly enabled features; opening/using core TrailCharter functionality does not create background network activity.
- 2026-08-28: Agreed local-only backup/export model: no TrailCharter cloud backup service, Android automatic cloud backup disabled/excluded as far as practical, backups created locally on user request, and destinations selected through Android Storage Access Framework.
- 2026-08-28: Agreed least-privilege contextual permissions with no startup permission barrage, preference for Photo Picker/Storage Access Framework over broad media/storage access, background location deferred, and a clear Privacy Status location-tracking On/Off state.
- 2026-08-28: Implemented the first Android application scaffold with a Compose single-activity shell, DataStore seam, provisional Privacy Status screen, local-backup exclusions, cleartext-network blocking and no `INTERNET`, location or broad-storage permissions.
- 2026-08-28: Generated and checksum-verified the Gradle `8.13` wrapper through an isolated bootstrap workflow, then removed the one-time bootstrap workflow once the wrapper was committed.
- 2026-08-28: Bootstrap CI run #6 passed `testDebugUnitTest`, `lintDebug` and `assembleDebug`.
- 2026-08-28: PR #1 (`Add TrailCharter Android foundation`) passed the permanent Android CI gate and produced debug artifact `trailcharter-debug-run-2`.
- 2026-08-28: Merged PR #1 into `main` at `2099d1198875f2dad624ecae86dff2bbcb04f5fc`.
- 2026-08-28: Post-merge Android CI run #3 passed unit tests, lint and debug assembly and produced `trailcharter-debug-run-3` (artifact digest `sha256:66863ec48d90cffc6dc9a68122db6059402a8babf6614f148ba2eab2fecd480b`).
