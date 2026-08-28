# TrailCharter Decisions

## FINAL
### Seasonal icon system
- Remove shield/container.
- Preserve the same core mountains/trees/river/sun composition.
- Expand/crop that artwork to fill circle, square and rounded-square masks.
- No extra trees, changed mountains, new river shape or restyling.
- Spring: fresher, lighter, less green-dominant.
- Summer: approved current palette.
- Autumn: retain green, tinged with restrained orange/gold.
- Winter: approved current palette.

## AGREE
- **TrailCharter is the current agreed product name.** It remains AGREE rather than FINAL until deliberately locked for public/release identity.
- **Current geographic scope:** TrailCharter's current product and release scope is the United Kingdom only: England, Scotland, Wales and Northern Ireland. Crown Dependencies and other countries are outside the current scope. Expansion beyond the UK may be reconsidered later.
- **Current live-sharing scope:** live location sharing is not part of the current product/release scope and will not be implemented in the present development plan. Location tracking remains local to the device. Live sharing may only be reconsidered in a later scope review.
- **Zero-owned-server architecture:** core TrailCharter functionality must not depend on servers owned or administered by the project. Core data and computation remain device-resident. External services may be used only where a feature inherently requires public/external data or distribution, and those dependencies should be isolated and replaceable.
- **Android application identity:** namespace and application ID are both `com.masamunr.trailcharter`.
- **Android SDK baseline:** `minSdk = 28`, `targetSdk = 36`, `compileSdk = 36`.
- **Android build toolchain:** Android Gradle Plugin `8.13.2`, Gradle `8.13`, Kotlin `2.3.21`, JDK `17`, Kotlin DSL build scripts, and a Gradle version catalog (`libs.versions.toml`) for dependency/version management.
- **Compose/UI foundation:** Jetpack Compose-first, Compose BOM `2026.06.00`, Material 3 as the interaction/accessibility foundation, single-activity architecture, edge-to-edge layouts, adaptive/responsive layouts from inception, accessibility-first components, and no ordinary XML screen layouts.
- **TrailCharter design system:** TrailCharter owns its colours, typography, shapes and component styling. Seasonal palettes are controlled by TrailCharter; Android dynamic colour is not enabled by default. Optional system-colour support may be explored later.
- **Navigation remains EXPLORE:** the Android foundation must keep navigation replaceable until the Adventure UX model is agreed.
- **Local-first persistence:** Room is the structured-data store; DataStore is used for settings/preferences; GPX, attachments and later offline/map packages are stored as app-managed files with structured metadata/references where appropriate.
- **Persistence boundaries:** UI code does not access storage directly. Data access is exposed through repository/interfaces so underlying storage/import mechanisms remain replaceable.
- **Database generation/migrations:** use KSP for generated database code; maintain explicit versioned Room migrations from the beginning; destructive production migrations are not permitted.
- **Local authority:** personal/core Adventure data is stored locally as the authoritative copy and core functionality must not depend on a cloud/backend service.
- **Secrets:** credentials or cryptographic key material, if later required, use Android Keystore-backed handling rather than ordinary preferences/database fields.
- **Network-silent by default:** opening and using core TrailCharter functionality must not create network activity by default. The initial application should not declare `INTERNET` permission. When network-dependent features are later introduced, network access must remain behind a defined TrailCharter network layer and occur only for clear user-initiated or explicitly enabled functions. Merely opening the app must not create background connections.
- **Network privacy:** no advertising, analytics, telemetry or third-party tracking SDKs; no silent uploads. When networking is introduced, cleartext HTTP is disabled and ordinary network traffic uses secure transport.
- **Local-only backup:** TrailCharter does not provide or depend on a TrailCharter cloud backup service and automatic cloud backup of personal/core data is disabled/excluded as far as the Android platform permits.
- **User-controlled backup/export:** backup files are created locally and explicitly by the user. Through Android's system document picker/Storage Access Framework, the user may save or copy that local backup to any destination/provider they already control, including personal services such as Proton Drive or OneDrive where exposed by Android. TrailCharter does not require a TrailCharter account or direct cloud integration for this.
- **Permissions:** least privilege; request permissions only in context when the user invokes a feature that needs them; no startup permission barrage; features degrade gracefully when permissions are denied. Prefer Android Photo Picker and Storage Access Framework over broad media/storage permissions. Background location requires a separate future decision.
- **Privacy Status:** TrailCharter will provide clear in-app status information for location tracking, including an unambiguous `On` / `Off` state. More detailed privacy-status presentation can evolve with later features.
- **CI/build pipeline:** GitHub Actions is the Android CI platform. Android/build-file changes on pull requests and pushes to `main` run on `ubuntu-latest` with JDK 17 and the committed Gradle Wrapper. CI must pass `testDebugUnitTest`, `lintDebug` and `assembleDebug`; successful runs upload a debug APK artifact. The official Gradle setup action handles dependency caching and Gradle Wrapper validation.
- **CI security/release separation:** normal CI/debug builds use no production signing secrets and do not publish automatically to Google Play. Release signing and store publishing remain separate controlled concerns. Workflow permissions remain read-only unless an explicit future task requires more.
- TrailCharter continues the same project history previously discussed under Altrove and Adventure.
- Android-first, privacy-first, offline-first where practical, open-source intended.
- OpenStreetMap foundation.
- Organic Maps may be reused/integrated but TrailCharter must retain its own identity.
- No core account, ads, default telemetry or silent uploads.
- Transparency by Design.
- GitHub is the authoritative source of truth.
- Android implementation is now open as a controlled, iterative foundation-development phase alongside continued product and architecture design.
- Foundation implementation must not prematurely lock unresolved EXPLORE decisions into the product or architecture.
- Early implementation should prioritise the Android project foundation, privacy/offline principles, local-first structure, branding/theming foundations, CI/build reliability and replaceable architectural seams before complex mapping/routing features.

## SUPERSEDED / REJECTED
- Previous AGREE direction: "No implementation until vision and architecture are sufficiently understood." Superseded on 2026-08-27 by the controlled-foundation implementation decision.
- Previous AGREE direction: live location sharing as an in-principle product capability. Superseded on 2026-08-28 by the decision to remove live location sharing from the current product/release scope.
- **Altrove as the product name is rejected/superseded** after clearance identified a directly adjacent active travel-planning app using the name. The repository remains `Masamunr/altrove` for continuity until a later deliberate repository rename.

## EXPLORE
- Adventure IA
- primary navigation model
- optional Android system/dynamic colour mode
- enhanced application-level encryption/threat model beyond normal Android app sandbox/device encryption
- exact TrailCharter backup archive format and optional user-controlled backup encryption
- Organic Maps technical relationship
- UK offline map architecture and package/distribution approach
- safety tools
- pack/food/water models
- journal model
- exact V1 feature scope within the UK boundary
- future expansion beyond the UK
- possible future reconsideration of live location sharing outside the current scope
