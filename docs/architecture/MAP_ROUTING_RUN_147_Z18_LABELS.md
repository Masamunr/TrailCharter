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

Examples expected to benefit where source data carries the names include well-known Eryri routes such as **Pyg Track** and **Watkin Path**.

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

## EXPLORE: hiking-route relation labels

A named OSM hiking route may span several constituent ways and may be represented more reliably by a route relation than by way-level `name` tags. If physical testing shows important named routes missing or fragmented, investigate extracting named hiking-route relations into the desktop-built regional map package.

Do not add a separate relation payload merely for theoretical completeness. First test whether the existing named-way labels provide adequate walking-map coverage.

## Next physical acceptance focus

1. confirm z18 zoom is genuinely useful and the existing vertical Zoom control reaches it;
2. confirm hillshade disappears cleanly above its useful native range without leaving an objectionable visual transition;
3. inspect the small BRouter/path offsets at z17-z18 and determine whether the route remains on the established routable corridor;
4. verify useful path names appear where source data contains them, without excessive label clutter;
5. confirm larger route labels are readable at ordinary planning zooms without excessive collision or repetition;
6. retain adjustable route opacity so route geometry can still be compared directly with the path beneath it.
