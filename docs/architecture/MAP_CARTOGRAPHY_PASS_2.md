# Offline map cartography pass 2

Status: **AGREE / SPIKE**

This note records the next agreed physical map pass after Cartography Pass 1 was accepted on-device. It remains isolated in the separate Map Spike and does not select a production renderer/routing engine.

## Physical evidence carried forward

- Run #72 physically renders the offline Eryri basemap and hillshade stably.
- The earlier mixed-geometry water artefacts are resolved to the user's satisfaction.
- At walking-scale zooms, the current hillshade is visibly soft because the embedded Mapterhorn DEM currently contains native terrain only through z12 and is being overzoomed above that level.
- The current two-finger pitch gesture is considered too fiddly to be the primary way to change map angle.

## AGREE: Cartography Pass 2

1. Increase native map detail for the Eryri spike:
   - vector basemap from z14 to z15;
   - terrain from z12 to z15 using Mapterhorn's high-resolution regional PMTiles shard merged with the global low-zoom extract;
   - retain explicit package-size measurement so quality/storage trade-offs are visible rather than guessed.
2. Add simple one-tap view-angle controls as the primary pitch interface:
   - Flat: 0 degrees;
   - Low angle: approximately 30 degrees;
   - Terrain angle: approximately 50 degrees;
   - compass/reset action restores north and flat orientation where appropriate;
   - two-finger tilt/rotation may remain available as optional gestures but must not be required.
3. **True 3D terrain mode is now an AGREE capability requirement.** TrailCharter must eventually provide an actual terrain-mesh 3D view, not merely relabel a pitched 2D hillshade map as 3D.
4. Current MapLibre Native Android does not support the MapLibre Style Spec `terrain` root property for true DEM terrain extrusion. Therefore the present MapLibre Android renderer may continue to serve the proven offline 2D/topographic map, but a dedicated technical path for true 3D terrain must be investigated separately rather than faked with camera pitch.
5. Building `fill-extrusion` is supported by MapLibre Native Android, but extruded buildings alone do not satisfy the agreed 3D terrain requirement.
6. Preserve the proven privacy path unchanged: `MapLibre.setConnected(false)`, local PMTiles, no INTERNET/ACCESS_NETWORK_STATE/location permissions, app-managed offline packages and final-APK permission verification.

## Acceptance for this pass

- Hillshade/terrain detail is visibly sharper at route-planning zooms than the z12 build.
- The added terrain detail does not introduce instability, obvious seams or unacceptable APK/package growth.
- Flat/Low/Terrain view presets work predictably with one tap and do not require two-finger pitch gestures.
- Compass/reset behaviour is clear and deterministic.
- The UI does not label pitched hillshade as true 3D terrain.
- The true 3D terrain capability remains explicitly tracked as a required follow-on renderer/engine investigation.
- Final APK remains network/location silent and installs only as the separate Map Spike.
