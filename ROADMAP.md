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

Remaining initial foundation scope:
- local-first persistence boundary
- privacy/network guardrails
- backup/export behaviour
- CI/build pipeline
- branding/theme implementation
- replaceable architectural seams for unresolved map/routing choices

Do not prematurely implement complex mapping, routing or live-sharing architecture before the relevant EXPLORE decisions mature.

## Later / progressive capability phases
- map foundation
- offline regions
- GPX
- itinerary/stages
- privacy status
- equipment/weight
- food/water
- safety
- live sharing
- journal
