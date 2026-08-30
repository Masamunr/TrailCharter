# Offline map cartography pass 2

Status: **AGREE / SPIKE / PHYSICALLY ACCEPTED**

This note records the agreed Cartography Pass 2 and its physical acceptance. It remains isolated in the separate Map Spike and does not select a production renderer/routing engine.

## Physical evidence carried forward

- Run #72 physically renders the offline Eryri basemap and hillshade stably.
- The earlier mixed-geometry water artefacts are resolved to the user's satisfaction.
- At walking-scale zooms, the original hillshade was visibly soft because the embedded Mapterhorn DEM contained native terrain only through z12 and was being overzoomed above that level.
- The original two-finger pitch gesture was considered too fiddly to be the primary way to change map angle.

## AGREE: Cartography Pass 2

1. Increase native map detail for the Eryri spike:
   - vector basemap from z14 to z15;
   - terrain above z12 using Mapterhorn's high-resolution regional PMTiles shard merged with the global low-zoom extract;
   - retain explicit package-size measurement so quality/storage trade-offs are visible rather than guessed.
2. Add simple one-tap view-angle controls as the primary pitch interface:
   - Flat: 0 degrees;
   - Low angle: approximately 30 degrees;
   - Terrain angle: approximately 50 degrees;
   - compass/reset action restores north and flat orientation where appropriate;
   - two-finger tilt/rotation may remain available as optional gestures but must not be required.
3. **True 3D terrain mode is an AGREE capability requirement.** TrailCharter must eventually provide an actual terrain-mesh 3D view, not merely relabel a pitched 2D hillshade map as 3D.
4. Current MapLibre Native Android does not provide the required true DEM terrain extrusion path for this spike. The present MapLibre Android renderer may therefore continue to serve the proven offline 2D/topographic map, while a dedicated technical path for true 3D terrain is investigated separately rather than faked with camera pitch.
5. Building `fill-extrusion` support does not satisfy the agreed 3D terrain requirement.
6. Preserve the proven privacy path unchanged: `MapLibre.setConnected(false)`, local PMTiles, no INTERNET/ACCESS_NETWORK_STATE/location permissions, app-managed offline packages and final-APK permission verification.

## Pass 2 physical acceptance

The initial full-region z15 terrain package in Run #75 proved the data path but produced a roughly 361 MiB APK and was not a practical physical install candidate.

Run #79 corrected the package shape to full Eryri terrain through z12 plus a controlled central Yr Wyddfa z13-z15 high-resolution area. The resulting APK was 164,819,900 bytes and installed and ran successfully on the physical Android 16 Motorola Edge 50 Pro on 28/08/2026.

The user reported that Run #79 works well overall. This satisfies the Cartography Pass 2 physical evidence gate. It does not make the package shape or pitch-preset UI FINAL.

## Acceptance result

- Higher-resolution terrain packaging is physically viable when regionally bounded rather than applied blindly across the full Eryri spike.
- The added terrain detail does not introduce a reported stability or permission regression.
- Flat/Low/Terrain pitch controls work physically, but the user prefers a slider-based camera-control design for the next pass.
- Compass/reset behaviour remains part of the required control model.
- The UI does not label pitched hillshade as true 3D terrain.
- The true 3D terrain capability remains explicitly tracked as a required follow-on renderer/engine investigation.
- Final APK remains network/location silent and installs only as the separate Map Spike.

The next agreed refinement is recorded in `MAP_CARTOGRAPHY_PASS_3.md`.
