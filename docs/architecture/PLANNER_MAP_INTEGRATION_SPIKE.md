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

## Routing boundary

The planner must not couple itself directly to BRouter as a product architecture. BRouter remains an EXPLORE routing engine behind the existing routing boundary. This spike may invoke the BRouter implementation for physical evidence, but production planner state must remain engine-agnostic.

## Physical acceptance focus

1. Open `Plan on map` from the stage-planning shell.
2. Select Start and tap the map; confirm the marker appears where intended.
3. Select Finish and tap the map; confirm the marker appears where intended.
4. Add at least one Waypoint and confirm ordered waypoint count/marker behaviour.
5. Turn Snap OFF and confirm the UI explicitly says exact coordinates are retained and guided calculation is disabled rather than silently snapping.
6. Turn Snap ON and calculate a WALK route using installed BRouter data; inspect route geometry against mapped paths.
7. Choose `Use stage route` and confirm the planner shell receives the selected route summary.
8. Re-open `Edit route on map` and confirm the draft remains available during the current spike session.

Only after this interaction is accepted should route persistence be added to the Adventure/Stage database schema.
