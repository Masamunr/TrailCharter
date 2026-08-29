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

- prefer OSM-derived `route=hiking` / `route=foot` / walking-route relation names and references;
- keep extraction/build work on the desktop/package-factory side so Android runtime remains offline and lightweight;
- render relation-derived route names along the relevant route geometry at useful walking zooms;
- deduplicate or suppress relation labels where the same route name is already satisfactorily supplied by a named way;
- never infer or manufacture a route name when source data is ambiguous;
- use **Watkin Path** as an explicit physical acceptance case for the next build.

## AGREE: incremental Eryri geography expansion

Grow the test geography gradually rather than jumping from the current Yr Wyddfa-centred package to a whole-country package.

Previous spike bounds were approximately:

`-4.22, 52.97, -3.95, 53.18`

Pass 4 extends the eastern edge to approximately **-3.88**, keeping the other bounds stable. The intended additional test area is **Capel Curig / Moel Siabod** and its surrounding walking network.

The purpose is not merely to add more map for its own sake. Each incremental expansion must record how package growth affects:

- total map-package size and each major payload;
- desktop package build time where practical;
- Android import/validation behaviour;
- map load and interaction responsiveness on-device;
- close-zoom rendering behaviour;
- routing-package coverage and route calculation behaviour where the routing data needs to grow as well.

Do not reduce agreed cartographic quality merely to hold the package to an arbitrary size. Use measured size/performance evidence to determine sensible future regional-package boundaries.

## Run #160 CI evidence: Eryri East Pass 4

Run #160 / isolated map-spike **versionCode 21** is fully green at code commit `c0f2549f457262403f9a035bfc88203a87f4e3f6`.

CI confirms:

- the expanded map-package bounds are `-4.22, 52.97, -3.88, 53.18`;
- schema 2 adds a dedicated OSM-derived hiking/walking relation GeoJSON payload;
- the generated relation payload contains **21 named route relations** and explicitly contains **Watkin Path**;
- the standalone physical-test map package is **183,152,066 bytes** as downloaded from the successful Run #160 artifact;
- the relation overlay itself is **591,980 bytes**, so the label-relation data is negligible compared with the terrain payload;
- the BRouter routing package remains unchanged and continues to use `W5_N50.rd5`; no routing-data growth was required for Capel Curig / Moel Siabod;
- tests, lint, APK assembly, no-embedded-map/routing checks, version/privacy gates and continuity signing all pass;
- the spike retains z18 inspection zoom, the z16.25 hillshade ceiling, adjustable planned-route opacity and the hard guided-route safety rule.

This was **CI/build evidence only** at the time of Run #160. The first physical pass is recorded below.

## Run #160 physical findings and diagnosis

Physical testing confirmed that the relation-derived **Watkin Path** label is present. It also found several brown dashed walking paths apparently ending and restarting in unrelated fragments.

The broken dashed geometry is a **basemap path-style filtering defect**, not malformed hiking-relation geometry, relation member ordering, package clipping or relation-layer ordering:

- the hiking-relation GeoJSON is consumed only by the `hiking-route-relation-labels` symbol layer; it does not draw a line and therefore cannot create or erase the brown dashed basemap paths;
- the relation payload deliberately retains OSM way members as `MultiLineString` geometry so MapLibre can place source-derived names along their real geometry; member order can affect label-placement opportunities but not the separately rendered basemap path line;
- the `tracks` / `paths` style used a narrow `kind_detail` allow-list, while Protomaps classifies the complete walking family under `kind=path` and uses values such as `footway`, `pedestrian`, `track`, `path`, `bridleway` and `steps` in `kind_detail`;
- valid adjacent `footway` / `pedestrian` sections were therefore omitted when their detail value changed, which matches the physical appearance of paths breaking at seemingly random way boundaries;
- the defect occurs within the installed package bounds and no relation line is layered above the path network, ruling out package-edge clipping and relation style occlusion.

The versionCode 22 correction selects the complete Protomaps `kind=path` family, keeps tracks visually distinct, and excludes only deliberately non-walking-map linework such as piers from the ordinary path layer. The same schema-level classification is applied to named walking-way labels. This is an APK style correction: **the accepted Eryri East Pass 4 map package does not need rebuilding or re-importing**.

The same physical session initially showed no routing controls after the Eryri East map import. Closing and reopening the app then exposed both **Yr Wyddfa** and **Moel Siabod** WALK tests. The restart evidence identifies a separate lifecycle defect: `BRouterRoutingSpikeHost` searched the Android view tree for at most eight seconds, so a large first-time map import could finish after the host had permanently stopped looking for its MapView. VersionCode 22 removes the timed view-tree search; the renderer now explicitly hands the ready MapLibre map to the routing host, so both fixed scenarios appear as soon as the imported map style is ready. A unit contract preserves both scenario definitions and the existing route-opacity control remains unchanged.

The first Moel Siabod WALK result also appeared not to follow an established route for part of its length. This is recorded as **unresolved routing-safety evidence** rather than accepted BRouter behaviour. Because the same build was omitting legitimate mapped path sections, that observation cannot yet distinguish a genuine off-network BRouter result from a renderer omission. Retest versionCode 22 with route opacity reduced and z17-z18 inspection. If the calculated line still leaves the now-continuous mapped network, it remains a routing-safety failure requiring engine/profile investigation. BRouter remains **EXPLORE** and no route-specific shaping point or hard-coded Moel Siabod/Watkin exception has been added.

VersionCode 22 preserves the existing privacy boundary: no `INTERNET`, location or broad storage permissions, offline app-private package use, unchanged BRouter package/profile data, and a draft PR.

## Run #163 CI evidence: physical-finding corrections

Run #163 is fully green for the versionCode 22 application code at commit `262d51ab1080f2d9debe551e32f0306c27fb65d5`.

CI confirms:

- the Eryri East Pass 4 package rebuilt and passed schema, payload, hash, bounds and Watkin-relation verification;
- the unchanged BRouter WALK package rebuilt and passed engine/profile/payload verification;
- all 12 unit tests, lint and APK assembly pass, including contracts for the Protomaps `kind=path` filters and both physical routing scenarios;
- the APK contains no embedded map or routing package and remains below the package-split size ceiling;
- versionCode 22, the no-network/location/broad-storage-permission boundary and continuity signing all pass;
- APK, map package, BRouter package and Room-schema artifacts uploaded successfully.

The continuity-signed physical APK is **87,346,095 bytes** with SHA-256 `852a092d01b4afe4a12146f97e22adbead60842c66d8447333ffe382fee484eb`. The accepted Run #160 Eryri East map and BRouter packages may be retained because the correction changes APK rendering/host wiring only.

## Next physical acceptance focus

1. install versionCode 22 over Run #160 without replacing either offline package;
2. confirm the previously broken brown dashed paths now remain continuous across `footway` / `path` / `pedestrian` tagging boundaries;
3. confirm Watkin Path and other relation-derived labels remain readable without duplicate clutter;
4. confirm both Yr Wyddfa and Moel Siabod WALK buttons appear immediately after a first-time map import, without restarting the app;
5. rerun **Plas y Brenin → Moel Siabod**, reduce route opacity to approximately 25–35%, and inspect the questionable section at z17-z18;
6. classify any remaining route/path divergence against the now-complete rendered network while retaining the rule that guided/magnetic routes must not silently invent an off-network shortcut.

