# Routing safety principles

Status: **AGREE**

TrailCharter is an outdoor planning and navigation application. Route generation must therefore be treated as a safety-critical product behaviour rather than merely a shortest-path calculation.

## Guided routing

For walking/hiking routes that TrailCharter presents as generated or recommended guidance:

- generated route geometry must remain on known routable paths, tracks, roads or other explicitly routable ways represented in the routing dataset;
- the app must not invent a direct cross-country shortcut merely because it is geometrically shorter;
- if the routing engine cannot produce a credible on-network route, TrailCharter should fail the route calculation or clearly report that no suitable guided route is available rather than silently drawing an off-path alternative;
- route-engine acceptance requires physical comparison of generated geometry against the mapped path network in representative terrain, including steep and consequential terrain;
- route rendering must remain inspectable so the mapped path below it can be checked during development and testing.

A routing engine is not acceptable for production merely because it successfully returns a route. Route fidelity to the established network is a hard acceptance criterion.

## AGREE: waypoint placement and snapping

Start, finish and intermediate waypoint placement on the map is user-authored input. TrailCharter must not silently treat an exact map tap as a mistake and move it to a different location.

- users must be able to choose whether map-selected points **snap automatically to the routable network or remain exactly where placed**;
- snapping behaviour must be explicit and understandable, with no hidden correction of the user's selected coordinates;
- when snapping is enabled, TrailCharter may move the selected point to a nearby credible routable location and should make that adjustment visually apparent;
- when snapping is disabled, the exact selected coordinate is preserved;
- if an exact unsnapped point cannot be served by guided/magnetic routing, TrailCharter should report that limitation and offer clear alternatives such as enabling snap, moving the point, or using an explicitly manual/direct segment;
- the interface must not describe an unsnapped user-selected point as a user error merely because it is off the routable network;
- snapping may be offered as a convenience or recommendation, but the final placement choice belongs to the user.

This user-facing snapping choice is distinct from routing-engine safeguards used internally during controlled spike testing. Engine-side correction must never become a justification for silently changing coordinates that the user explicitly chose to keep exact.

## Manual planning

TrailCharter may allow the user to create a route manually, including a deliberately off-path/direct segment where appropriate. Manual geometry must be clearly distinguished from app-generated guidance so the product does not imply that TrailCharter has assessed or recommended that line as a safe established route.

The product model should preserve the distinction already represented by the routing contracts:

- **MAGNETIC / GUIDED**: TrailCharter routes over the known routable network and is responsible for not inventing arbitrary off-network shortcuts.
- **DIRECT / MANUAL**: the user deliberately chooses the geometry; TrailCharter should make that state visually and semantically clear.

## BRouter spike acceptance implication

The Run #141 physical test proved that opacity control makes the generated route inspectable, but it also exposed a short off-path excursion around the intermediate Pyg Track shaping point. That section is a **route-fidelity FAIL** until corrected.

The controlled BRouter spike may use misplaced-via correction to investigate engine behaviour while preserving the same route, profile and routing dataset. If an excursion remains, it counts directly against BRouter route quality. The spike must not hide or post-process an unsafe-looking route merely to make the engine appear successful.
