# Offline map cartography pass 1

Status: **AGREE / SPIKE**

This note records the physical-device evidence and the agreed next cartography work on the `spike/map-routing-boundaries` branch. It does not select a production routing engine and does not merge the spike into the normal TrailCharter app.

## Physical evidence through run #70

- MapLibre Native library initialisation passes on the physical Android 16 device.
- The original startup crash was traced to MapLibre's connectivity receiver querying `ConnectivityManager` after TrailCharter deliberately removed `ACCESS_NETWORK_STATE`. TrailCharter now calls `MapLibre.setConnected(false)` before map-surface creation, using MapLibre's supported offline connectivity override. No network-state permission was added.
- The blank-map diagnostic was separately traced to raw local style JSON being supplied to the URI-style `setStyle(String)` path. The local style now uses `Style.Builder().fromJson(...)`.
- With those two root causes corrected, direct SurfaceView and TextureView rendering both physically passed.
- Run #70 physically rendered a real offline Eryri extract using an embedded Protomaps/OSM vector PMTiles package plus an embedded Mapterhorn Terrarium DEM PMTiles package. Panning and zooming were stable in the reported test.
- The first real-map physical screenshots exposed blue triangular artefacts at higher zoom. Root cause: Protomaps v4 `water` contains mixed polygon and line geometry, while the spike applied a fill to the whole source-layer. The reference Protomaps style filters water fills to polygon geometry and handles river/stream lines separately.

## AGREE: next cartography pass

1. Restrict filled water to polygon geometry only.
2. Render rivers/canals and streams/drains as separate line layers.
3. Improve walking-scale hierarchy by separating major roads, minor roads, tracks and foot/cycle/bridle paths rather than styling almost all transport lines identically.
4. Make data zoom ceilings explicit: the current Eryri vector extract is z0-z14 and the Mapterhorn DEM extract is z0-z12. Controlled overzoom is acceptable for this spike, but it must not be mistaken for additional native terrain detail.
5. Keep the physically proven renderer path, `MapLibre.setConnected(false)`, local `Style.Builder().fromJson(...)`, app-managed PMTiles storage and the final-APK permission guard unchanged.
6. Continue to investigate contours as a separate topo enhancement. The current embedded Mapterhorn archive is raster DEM, so contour generation needs an explicit offline/native strategy rather than pretending contour vectors already exist in the package.

## Acceptance for this pass

- No angular/triangular water fill artefacts when zooming in.
- Lakes/reservoirs remain filled correctly.
- Rivers/streams read as lines rather than filled shapes.
- Major/minor roads, tracks and walking paths are visually distinguishable at route-planning scale.
- Hillshade remains stable through the supported spike zoom range, with the z12 terrain ceiling documented and explicit in style configuration.
- No INTERNET, ACCESS_NETWORK_STATE, Wi-Fi-state or location permission appears in the final APK.
- No change to the normal TrailCharter application/data package; the work remains in the separate Map Spike.
