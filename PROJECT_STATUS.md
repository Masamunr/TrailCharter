# TrailCharter Project Status

## Phase
**Product Design + Foundation Development**

## Product name
**TrailCharter — AGREE**

The name is agreed for current development and branding work but is not yet FINAL. The existing repository path `Masamunr/altrove` is retained for continuity until a later deliberate rename decision.

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

## Implementation
Android implementation is authorised as a controlled foundation phase.

No production Android application has been implemented yet.

## Current state
- Project history consolidated.
- Constitution established.
- Vision established.
- Decisions classified into EXPLORE / AGREE / FINAL.
- TrailCharter selected as the current agreed product name following deeper collision/clearance checks.
- Android package/application identity agreed as `com.masamunr.trailcharter`.
- Android SDK baseline agreed as API 28 minimum and API 36 target/compile.
- Android build toolchain agreed as AGP 8.13.2 / Gradle 8.13 / Kotlin 2.3.21 / JDK 17 with Kotlin DSL and version catalog.
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
- Treat mapping/routing architecture as an investigation boundary until the Organic Maps/OpenStreetMap approach is properly resolved.

## Next priorities
1. Define remaining Android foundation choices: Compose/UI structure, local persistence boundary, privacy/network rules and CI/build pipeline.
2. Preserve the exact FINAL launcher icon reference asset in GitHub.
3. Continue product identity beyond the launcher icon: wordmark, typography, in-app visual design language and seasonal theme application.
4. Define the Adventure information model.
5. Define the main UX/navigation model.
6. Investigate map/offline architecture.

## Implementation gate
**OPEN — AGREE**

TrailCharter may enter iterative foundation development alongside continued product and architecture design. Unresolved EXPLORE decisions remain open and must not be treated as settled merely because implementation has begun.
