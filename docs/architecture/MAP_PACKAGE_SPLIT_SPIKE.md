# Offline map package split spike

Status: **AGREE DIRECTION / EXPLORE FORMAT / SPIKE**

This note records the transition immediately after the final embedded-heavyweight Cartography Pass 3 comparison. It does not select a FINAL regional package format, a production map-distribution service, or a production routing engine.

## Physical evidence carried forward from Run #111

- Run #111 is the current CI-verified map-spike build at versionCode 15.
- Physical testing confirmed the revised vertical slider/toggle arrangement is substantially more intuitive.
- The slider/toggle cluster is now slightly too low. **AGREE:** move the whole cluster upward by approximately 2 cm in the next combined map/routing test build rather than producing a dedicated APK for this cosmetic adjustment.
- Do not reopen the accepted vertical orientation, background-free treatment, smaller thumb direction, Tilt/Zoom toggle arrangement, separate top-right compass, z16 terrain ceiling, contours, or ordinary map gestures unless new physical evidence requires it.
- The faint Run #105 hillshade joins were addressed by stopping raster-DEM overzoom beyond the genuine z16 terrain ceiling. The next combined build must preserve this behaviour and should be checked again physically rather than declaring the seam issue FINAL from CI alone.

## AGREE: separate the application from heavyweight regional map data

Cartography Pass 3 was deliberately the last test in which regional basemap, terrain and contours were embedded in the APK. The next architecture slice is to prove that:

1. heavyweight cartographic preparation can happen on a normal desktop PC;
2. the resulting regional package can be transferred to the phone explicitly;
3. TrailCharter can validate and copy/import the package into app-managed local storage;
4. the Android application itself remains comparatively small;
5. runtime map use remains fully offline and does not require a TrailCharter server, account or automatic/background networking;
6. basemap, terrain, contours, glyphs and later routing/walking-detail data remain independently replaceable behind TrailCharter boundaries.

This is a development/build preparation change, not a move to a server-backed product architecture.

## Desktop builder first slice

Add a cross-platform Python desktop builder under `tools/maps/` that reproduces the physically accepted Eryri Pass 3 inputs:

- Protomaps / OpenStreetMap basemap through native z15;
- Mapterhorn terrain covering full Eryri through z12, the established central area through z15 and the tighter summit/walking area through native z16;
- OS Terrain 50 10 m contours clipped to Eryri and converted to local PMTiles;
- the pinned local glyph PBF used for offline contour labels;
- deterministic SHA-256 and byte-size metadata for every payload.

The builder must write to a standalone output directory/container and must not require Android tooling. It may use the pinned `go-pmtiles` CLI and standard desktop Python libraries.

## EXPLORE: transport/container shape

For the first import proof, a ZIP containing `manifest.json` plus the independent PMTiles/glyph payloads is acceptable as a **spike transport container only**. The filename/extension and manifest schema are not FINAL product decisions.

The spike manifest should identify at minimum:

- schema version;
- package ID and display name;
- geographic bounds;
- payload path, byte count and SHA-256;
- layer type and native zoom ceiling where relevant;
- source/attribution metadata;
- explicit statement that runtime networking is not required.

The Android importer must reject missing, unexpected, path-traversal, oversized or hash-mismatched payloads before replacing the currently installed regional package.

## Android import proof: next implementation slice

After the desktop builder is committed and verified, replace embedded Pass 3 assets with an explicit Storage Access Framework import flow in the isolated `.mapspike` application:

1. app opens without a regional map package and explains that one must be imported;
2. user chooses the package through Android's system document picker;
3. package is validated before installation;
4. installation is atomic into TrailCharter-managed private storage;
5. the same accepted Eryri style renders from imported local files;
6. package remains available after app restart;
7. replacing a package cannot leave a half-installed map if validation/copy fails;
8. no broad storage permission is added;
9. no INTERNET/network/location permission is added by the package split.

For physical proof, CI should upload the small map-spike APK and the Eryri test package as separate artifacts.

## Acceptance evidence for the package split

- APK size falls materially because regional basemap/terrain/contours are no longer embedded.
- Eryri package imports through the system picker and renders identically enough to the accepted Pass 3 cartography for physical comparison.
- The 2 cm upward slider/toggle adjustment is included in this combined build.
- Imported package survives process death and normal app restart.
- Invalid/tampered package is rejected without damaging the previously installed valid package.
- Final APK remains free of INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, coarse location and fine location permissions.
- App does not make network requests while opening, importing or rendering a local package.
- Package byte sizes and hashes are recorded explicitly.
- The spike container/manifest remains EXPLORE until physical import evidence exists.

## Routing follows this boundary

The existing `RoutingEngineBoundary` remains authoritative. Once the package split is physically proven, start the previously recorded BRouter-first offline walking-routing comparison using representative Eryri routes. Routing data must be treated as another local package concern rather than being hidden back inside an ever-growing APK.

Valhalla remains the second comparison candidate. Neither becomes production-selected without physical route-quality and integration evidence.
