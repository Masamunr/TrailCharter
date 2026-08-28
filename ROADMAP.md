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

Remaining initial foundation scope:
- Compose/UI foundation
- branding and theme foundation
- local-first persistence boundary
- privacy/network guardrails
- CI/build pipeline
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
