# Run 147 routing review and z18/name-label refinement

Status: **AGREE / PHYSICAL EVIDENCE / SPIKE**

## Run #147 physical evidence

The corrected BRouter WALK package was physically tested on-device using the same fixed three-point route:

- Pen-y-Pass → Pyg Track via → Yr Wyddfa;
- route result: **5.43 km, +730 m / -82 m, 106 min**;
- the previous large off-path excursion seen in Run #141 disappeared after enabling BRouter `correctMisplacedViaPoints` with the standard 400 m correction distance;
- this is strong evidence that the large excursion was caused by the intermediate shaping point rather than a deliberate cross-country preference by the walking profile;
- smaller visual offsets remain between the calculated route and the dashed path rendering in some places.

The remaining small offsets are **not yet classified as routing-safety failures**. They may represent genuine graph/path divergence, or may reflect geometry/generalisation/snapshot differences between the BRouter routing dataset and the Protomaps basemap. They must be inspected more closely before BRouter receives stronger acceptance.

The hard routing-safety principles in `ROUTING_SAFETY_PRINCIPLES.md` remain unchanged: a guided/magnetic route must not silently invent an off-network shortcut.

## AGREE: close inspection zoom

For the next physical pass:

- increase the map camera maximum from z16 to **z18**;
- treat z18 as **inspection zoom only**, not additional native map or terrain resolution;
- retain the package's actual native ceilings: Protomaps vectors z15, Mapterhorn DEM z16, OS Terrain 50 contours z14;
- stop rendering the raster hillshade at approximately **z16.25** so z17-z18 inspection does not materially overzoom the DEM or amplify the known tile-edge seam;
- allow vector paths, contours and route geometry to overzoom to z18 so close route/path comparison is practical.

## AGREE: named walking paths and tracks

The accepted topo style should show reliable names for established walking paths/tracks when that name exists in the offline OSM-derived basemap.

First implementation:

- use the Protomaps v4 `roads` layer's OSM-derived `name` field;
- restrict labels to walking-relevant `kind_detail` values such as `path`, `track`, `bridleway` and `steps`;
- begin labels at approximately z14 and scale them modestly through z18;
- render names along the path with restrained spacing and collision handling;
- do not infer, manufacture or hard-code names where the map source does not contain a reliable name.

## AGREE: walking-route label readability

Physical testing of Run #151 confirms the named-track labels are useful but currently too small and too dependent on very close zoom.

Refine the walking-route label style so route names are readable during ordinary route planning rather than only at z17-z18:

- keep labels available from approximately z14;
- increase text size progressively with zoom, targeting roughly **12–13 px around z14**, **14 px around z16**, and **15–16 px by z18**;
- strengthen the pale text halo modestly so labels remain legible over contours and hillshade;
- tune line-label spacing/collision behaviour so larger labels do not simply disappear more often;
- repeat long route names sparingly rather than cluttering the map;
- give named walking paths/tracks useful visual priority while keeping them subordinate to an active planned-route overlay.

The acceptance target is that a walker can identify a named route at a normal planning zoom without needing to zoom all the way to z18.

## Run #151 physical evidence: Watkin Path label gap

Run #151 physical testing found that **Watkin Path was not labelled**, despite the general named-way label layer working elsewhere.

This is concrete evidence that way-level `name` tags alone do not provide adequate coverage for TrailCharter's walking-map use case. Well-known walking routes may be represented by OSM hiking/walking route relations even where their individual constituent ways do not carry the route name.

Do not work around this by hard-coding famous route names into the Android renderer.

## AGREE: hiking-route relation labels

For the next cartography spike, add reliable named hiking/walking route-relation support to the desktop-built regional map package where source data permits it.

Requirements:

- prefer authoritative OSM-derived `route=hiking` / walking-route relation names and references;
- keep extraction/build work on the desktop/package-factory side so Android runtime remains offline and lightweight;
- render relation-derived route names along the relevant route geometry at useful walking zooms;
- deduplicate or suppress relation labels where the same route name is already satisfactorily supplied by a named way;
- never infer or manufacture a route name when source data is ambiguous;
- use **Watkin Path** as an explicit physical acceptance case for the next build.

The implementation route remains a technical detail: first inspect whether the existing Protomaps source exposes sufficient relation-derived data; if not, add a small dedicated relation-derived vector layer during desktop package generation.

## AGREE: incremental Eryri geography expansion

Grow the test geography gradually rather than jumping from the current Yr Wyddfa-centred package to a whole-country package.

Current spike bounds are approximately:

`-4.22, 52.97, -3.95, 53.18`

For the next package pass, extend the eastern edge to approximately **-3.88**, keeping the other bounds broadly stable. The intended additional test area is **Capel Curig / Moel Siabod** and its surrounding walking network.

The purpose is not merely to add more map for its own sake. Each incremental expansion must record how package growth affects:

- total map-package size and each major payload;
- desktop package build time where practical;
- Android import/validation behaviour;
- map load and interaction responsiveness on-device;
- close-zoom rendering behaviour;
- routing-package coverage and route calculation behaviour where the routing data needs to grow as well.

Do not reduce agreed cartographic quality merely to hold the package to an arbitrary size. Use measured size/performance evidence to determine sensible future regional-package boundaries.

## Next physical acceptance focus

1. confirm larger walking-route labels are readable at ordinary planning zooms without excessive collision or repetition;
2. confirm **Watkin Path** is reliably labelled through route-relation support rather than hard-coding;
3. confirm the expanded Capel Curig / Moel Siabod area imports and renders at the same expected cartographic quality;
4. record the size increase versus the previous Eryri package and note any material import/rendering impact;
5. run at least one walking-route test in the newly added eastern area so BRouter is assessed outside the original Yr Wyddfa test corridor;
6. retain z18 inspection zoom and adjustable route opacity for route-fidelity review;
7. continue to enforce the routing-safety rule that guided/magnetic routes must remain on known routable network geometry.
