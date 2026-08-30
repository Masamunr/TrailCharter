# Planner ↔ map integration spike

Status: **AGREE / SPIKE**

## Baseline

Run #180 / map-spike versionCode 24 is physically accepted for the current path visual hierarchy:

- ordinary paths/tracks are readable without dominating;
- recognised walking routes stand out clearly;
- recognised routes become visible from a useful further-out zoom level;
- recognised and ordinary paths remain in the agreed violet family;
- TrailCharter-created routes remain visually distinct;
- potentially legitimate isolated path fragments are not hidden merely because they look disconnected.

This is the cartographic baseline for planner ↔ map integration.

## First integration slice

The first integration slice proves the interaction before changing the saved Adventure schema.

Flow:

`Stage route planning → Plan on map → choose Start / Finish / Waypoint → tap map → optional guided calculation → Use stage route → return to planner summary`

The route draft is deliberately in-memory in this spike. It does **not** alter persisted Adventure or Stage data yet. Persistence should be designed only after the interaction is physically accepted, so prototype assumptions do not leak into the production database.

## AGREE: direct map selection

- Start, finish and waypoint coordinates may be selected directly on the map.
- The user explicitly chooses which role the next map tap sets: Start, Finish or Waypoint.
- Selected points are visibly marked on the map.
- Waypoints are ordered in the order they are added.

## AGREE: snapping is user-controlled

- Snap to routable network is a user choice, not an automatic correction.
- **Snap ON:** guided/magnetic routing may use the routing engine's network correction/snapping behaviour.
- **Snap OFF:** TrailCharter preserves the exact coordinate selected by the user.
- TrailCharter must not silently move an unsnapped point or describe the user's deliberate selection as a mistake.
- In this first spike, guided BRouter calculation is deliberately disabled while Snap is OFF because BRouter would otherwise be allowed to alter the effective route endpoints. Manual/direct routing will handle deliberate exact/off-network geometry in a later slice.

## AGREE: map-first product home

- The production TrailCharter app opens directly on the map.
- The map is the primary home surface and default launch destination.
- Adventures, planning, saved routes, map packages, settings and related functions are accessed from the map UI rather than through a mandatory landing menu/dashboard.
- An active or recently opened Adventure may surface contextual information on the map without replacing the map as the home surface.
- Dedicated planner/detail screens remain appropriate where needed, but the natural return destination is the map.
- New-user/empty state remains map-first and may offer lightweight prompts such as `Plan an adventure` over the map.
- The current `Stage route planning` shell is spike scaffolding only and is **not** the intended production home architecture.

## Routing boundary

The planner must not couple itself directly to BRouter as a product architecture. BRouter remains an EXPLORE routing engine behind the existing routing boundary. This spike may invoke the BRouter implementation for physical evidence, but production planner state must remain engine-agnostic.

## Run #188 CI evidence

Run #188 at head `a77e5bf39c9ab26fb8a9fcbfa6d487a78420adeb` passed the complete Android CI gate: map package build/verification, BRouter package build/verification, unit tests, lint, debug APK assembly, no-embedded-map/routing checks, privacy permission checks and continuity signing.

The build intentionally reuses the accepted Run #180 `versionCode 24` build/workflow configuration. The code change is the planner ↔ map interaction spike; the offline map and BRouter package formats/data are unchanged. The same-signed APK is intended to replace the existing map-spike installation without clearing its app-private imported packages.

## Run #188 physical result

The first planner ↔ map interaction was physically tested and reported to work nicely. Treat the interaction slice as **PASS / accepted for progression to persistence design**, subject to the presentation refinement below.

Accepted behaviour includes:

- opening the map from the stage-planning shell;
- selecting start, finish and waypoint positions directly on the map;
- the explicit Snap on/off model;
- guided WALK calculation when snapping is enabled;
- returning the selected route draft to the stage-planning shell;
- retaining the in-memory route draft for editing during the current spike session.

### Duration presentation refinement

The routing calculation is valid, but the current UI exposes longer estimates as total minutes (for example `106 min`). The product display should use human-readable hours and minutes without changing the underlying duration value:

- under one hour: `42 min`;
- exactly one hour: `1 hr`;
- over one hour: `1 hr 46 min`;
- longer routes continue naturally, for example `5 hr 12 min`.

This is a presentation-only refinement and should be folded into the next planner/map slice rather than creating a standalone physical build.

## Persisted Stage-route slice

The accepted interaction advances in map-spike versionCode 25 to Room schema 3. This is an additive migration from schema 2; the existing `adventures`, `stages` and `itinerary_items` tables are unchanged.

Each Stage may own one current route through three new tables:

- `stage_routes` stores generic planning/travel semantics, the snap preference, distance, ascent, descent, duration and timestamps;
- `stage_route_control_points` stores exact user-selected Start, ordered Waypoints and Finish coordinates;
- `stage_route_geometry_points` stores the ordered displayed route geometry.

All route data is keyed to `stageId` and cascades when that Stage is deleted. Saving after editing or recalculation replaces the Stage's current control points and geometry in one Room transaction while retaining its original creation timestamp. There is no routing-engine name, profile, segment filename or BRouter-specific value in the schema. `MAGNETIC` / `DIRECT`, travel mode and snap preference are product-level semantics shared through the routing boundary.

The map-planning overlay now receives the generic `RoutingEngineBoundary`; only the spike host constructs the current BRouter EXPLORE implementation. A selected coordinate remains the persisted source of truth. In particular, Snap OFF stores the exact full-precision tapped coordinate, disables guided calculation, and explains that manual/direct routing will serve deliberate off-network geometry in a future slice.

The persisted route is observed from Room when its Stage is selected. Its points, metrics and full geometry can be reopened on the map, edited, recalculated and saved back to the same Stage. The Stage shell also exposes a focused create/select flow for physical persistence testing; it remains spike scaffolding and does not change the agreed map-first production home.

Duration remains stored as seconds and is now presented as human-readable minutes/hours (`42 min`, `1 hr`, `1 hr 46 min`, `5 hr 12 min`).

Migration and persistence evidence is deliberately layered:

- an SQLite migration test constructs the exported v2 schema, inserts representative Adventure/Stage/itinerary data, applies the additive route tables and proves the old rows survive;
- the v2 and v3 exported schemas are compared to prove all three previous entity definitions are unchanged;
- focused JVM tests cover ordered waypoint round-trip, full coordinate precision with Snap OFF, geometry/metric round-trip, route replacement, deletion and duration formatting;
- Room continues to export schema 3 for review and CI artifact retention.

The map and BRouter package formats/data are unchanged. BRouter remains **EXPLORE**, the route-safety principles remain binding, PR #13 remains draft, and the APK retains the no-INTERNET/location/broad-storage permission boundary.
