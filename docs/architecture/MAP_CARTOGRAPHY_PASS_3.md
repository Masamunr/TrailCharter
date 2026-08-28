# Offline map cartography pass 3

Status: **AGREE / SPIKE**

This note records physical acceptance of the corrective Cartography Pass 2 build and the next agreed map refinement. It remains isolated in draft PR #13 and does not select a production renderer, routing engine, offline-package scale or true-3D implementation.

## Physical evidence carried forward

- Run #79 installs and runs successfully on the physical Android 16 Motorola Edge 50 Pro.
- The reduced package strategy is physically viable: full-Eryri z0-z12 terrain plus a controlled central z13-z15 terrain area avoids the practical installation failure seen with the 361 MiB Run #75 package.
- The existing offline map, hillshade, water correction and view-angle behaviour work well enough to accept Cartography Pass 2 as physical evidence.
- The user still wants visibly finer detail when zoomed close in.
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
   - the existing package-size and final-APK size gates.
7. Do not claim additional vector detail by merely raising a style/source ceiling. The current Protomaps v4 basemap is natively z0-z15, so z16+ road/path detail requires a different source or an additional TrailCharter-built walking-detail vector layer.
8. If the accepted z16 terrain pass still leaves close-up roads/paths visibly too simplified, EXPLORE a small higher-detail walking vector overlay generated at build/package time from raw OpenStreetMap data, behind the existing offline-package boundary.
9. Continue the fixed privacy baseline unchanged: no INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, coarse location or fine location permissions; MapLibre remains forced disconnected before map creation.
10. True 3D remains a separate agreed capability with implementation EXPLORE. Camera tilt is not to be described as true 3D terrain.

## Acceptance for this pass

- Run #79 remains the accepted physical baseline and is not regressed.
- The map spike still installs beside normal TrailCharter and updates deterministically.
- The slider makes tilt and zoom materially easier to control than the previous preset-button stack.
- Switching Tilt / Zoom does not jump or reset the other camera property.
- Slider values stay synchronized after pinch/tilt gestures.
- North/reset remains easy to reach and deterministic.
- Within the z16 test area, hillshade/relief is visibly sharper at close walking-scale zoom than Run #79.
- Outside the z16 test area, the transition back to the z15/z12 hierarchy is stable and does not produce blank relief or corruption.
- Terrain and APK sizes remain inside the spike's hard CI gates; if not, CI fails rather than producing another oversized physical-test APK.
- Final APK remains network/location silent.

## Data-source boundary

Mapterhorn's regional PMTiles shards provide native terrain from z13 through z17 and are suitable for the controlled z16 terrain probe. Protomaps v4 planet basemaps contain native vector tiles only through z15. These are different ceilings and must remain explicit in both implementation and documentation.

A future high-detail walking overlay, if required, should be generated deterministically from open OSM-derived source data at package-build time and should contain only the extra walking-scale geometry needed to justify its storage cost.
