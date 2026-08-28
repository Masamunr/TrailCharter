# Offline map cartography pass 3

Status: **AGREE / SPIKE**

This note records physical acceptance of the corrective Cartography Pass 2 build and the next agreed map refinement. It remains isolated in draft PR #13 and does not select a production renderer, routing engine, offline-package scale or true-3D implementation.

## Physical evidence carried forward

- Run #79 installs and runs successfully on the physical Android 16 Motorola Edge 50 Pro.
- The reduced package strategy is physically viable: full-Eryri z0-z12 terrain plus a controlled central z13-z15 terrain area avoids the practical installation failure seen with the 361 MiB Run #75 package.
- The existing offline map, hillshade, water correction and view-angle behaviour work well enough to accept Cartography Pass 2 as physical evidence.
- The user still wants visibly finer detail when zoomed close in.
- The user considers contours increasingly important to support terrain reading at walking scale rather than relying on hillshade alone.
- The one-tap Flat / Low / Terrain buttons work, but are no longer the preferred camera-control design.

## AGREE: Cartography Pass 3

1. Replace the separate pitch-preset buttons with one compact camera slider.
2. The slider has two modes selected by a compact two-state control:
   - **Tilt** controls map pitch continuously from flat to the renderer's useful maximum;
   - **Zoom** controls camera zoom continuously across the useful offline-map range.
3. Preserve ordinary map gestures. The slider is an accessible/direct alternative rather than a replacement for pinch, pan, rotate or optional two-finger tilt.
4. Keep a compact deterministic north/reset action available without restoring the previous stack of pitch buttons.
5. Slider state should track the live map camera, including camera changes made by gestures, so the control never displays a stale value.
6. Increase genuine close-up terrain detail by adding **native z16 Mapterhorn terrain only over a tighter central walking/summit test area**, while retaining:
   - full Eryri terrain through z12;
   - the existing controlled central terrain through z15;
   - explicit package-size measurement and a generous spike-only safety ceiling.
7. Add **real vector contours** to the Eryri spike in this pass rather than leaving them as a later research item:
   - use OS Terrain 50 contour data for the Great Britain prototype;
   - preserve the native 10 m contour interval;
   - style ordinary contours lightly so they support rather than obscure roads, paths and hillshade;
   - distinguish index contours, initially every 50 m, with slightly stronger weight;
   - add restrained elevation labels to index contours at useful walking-scale zooms;
   - clip/package contours at build time and keep them fully local/offline;
   - package as a dedicated local vector layer/archive so contour data remains replaceable independently of the Protomaps basemap and Mapterhorn DEM.
8. Contour styling must remain readable with hillshade. If the combination becomes visually noisy, reduce label density/line emphasis by zoom rather than removing useful terrain information.
9. Do not claim additional vector detail by merely raising a style/source ceiling. The current Protomaps v4 basemap is natively z0-z15, so z16+ road/path detail requires a different source or an additional TrailCharter-built walking-detail vector layer.
10. If the accepted z16 terrain + contour pass still leaves close-up roads/paths visibly too simplified, EXPLORE a small higher-detail walking vector overlay generated at build/package time from raw OpenStreetMap data, behind the existing offline-package boundary.
11. Continue the fixed privacy baseline unchanged: no INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, coarse location or fine location permissions; MapLibre remains forced disconnected before map creation.
12. True 3D remains a separate agreed capability with implementation EXPLORE. Camera tilt and contours are not to be described as true 3D terrain.

## Why contours are now part of Pass 3

Run #79 demonstrates that simply increasing raster elevation resolution has diminishing value if the map still lacks explicit topographic structure. Hillshade is excellent for intuitive shape, but contours provide deterministic slope/height information and make ridges, cols, bowls and steepness legible at the exact scales used for walking route planning. The next test therefore needs to judge the combined effect of **higher-resolution DEM + hillshade + real contours**, not DEM resolution in isolation.

## Final embedded-package comparison

Pass 3 is deliberately the **last map-quality experiment in which the heavyweight regional map data is embedded inside the test APK**.

The user wants to see the combined visual impact of z16 relief, real contours and the revised camera control before changing the development plumbing. Therefore APK/map-package size remains measured and bounded in CI, but the previous tight Run #79 limits must not dictate the visual experiment. Pass 3 uses generous safety ceilings intended only to catch an accidental runaway package; they are not production size targets and do not constitute a decision to ship maps inside the application package.

After Pass 3 physical testing, AGREE to move heavyweight cartographic preparation onto the user's PC and separate the application from regional offline map packages. The intended next technical direction is:

- keep TrailCharter itself comparatively small;
- generate/clip/package basemap, terrain, contours and later walking-detail layers on the PC;
- transfer/import regional packages to the phone explicitly;
- validate and store imported packages locally in TrailCharter-managed storage;
- keep packages independently replaceable and measurable;
- continue to test runtime storage, import time, RAM/GPU use and pan/zoom performance on the physical Android device;
- preserve all existing privacy/offline decisions: no TrailCharter server, no account, no automatic/background network activity, and no new runtime network permission merely to obtain map data.

This sequencing prevents APK delivery constraints from prematurely deciding cartographic quality while retaining device performance and practical storage as real engineering constraints.

## Acceptance for this pass

- Run #79 remains the accepted physical baseline and is not regressed.
- The map spike still installs beside normal TrailCharter and updates deterministically.
- The slider makes tilt and zoom materially easier to control than the previous preset-button stack.
- Switching Tilt / Zoom does not jump or reset the other camera property.
- Slider values stay synchronized after pinch/tilt gestures.
- North/reset remains easy to reach and deterministic.
- Within the z16 test area, hillshade/relief is visibly sharper at close walking-scale zoom than Run #79.
- 10 m contours make slope, ridges, bowls and height changes materially easier to read without making the map cluttered.
- Index contours and elevation labels are distinguishable but subordinate to route/path information.
- Contours remain correctly aligned with the rendered terrain and map features at all tested zooms.
- Outside the z16 test area, the transition back to the z15/z12 hierarchy is stable and does not produce blank relief or corruption.
- Terrain, contour and APK sizes are measured explicitly and remain inside the generous Pass 3 safety bounds; exceeding them fails CI and prompts investigation rather than automatic quality reduction.
- Final APK remains network/location silent.

## Data-source boundary

Mapterhorn's regional PMTiles shards provide native terrain from z13 through z17 and are suitable for the controlled z16 terrain probe. Protomaps v4 planet basemaps contain native vector tiles only through z15. OS Terrain 50 provides the Great Britain contour source for this controlled experiment. These are separate data layers with separate ceilings and should remain replaceable independently.

For a later UK-wide implementation, Northern Ireland still requires the separately recorded OSNI/LPS contour-generation path rather than pretending a Great Britain dataset covers the whole release geography.

A future high-detail walking overlay, if required, should be generated deterministically from open OSM-derived source data at package-build time and should contain only the extra walking-scale geometry needed to justify its storage cost.
