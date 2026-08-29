# Offline map package split spike

Status: **AGREE DIRECTION / EXPLORE FORMAT / SPIKE**

This note records the transition immediately after the final embedded-heavyweight Cartography Pass 3 comparison. It does not select a FINAL regional package format, a production map-distribution service, or a production routing engine.

## Physical evidence carried forward from Run #111

- Run #111 is the final embedded-heavyweight map-spike build at versionCode 15.
- Physical testing confirmed the revised vertical slider/toggle arrangement is substantially more intuitive.
- The slider/toggle cluster is now slightly too low. **AGREE:** move the whole cluster upward by approximately 2 cm in the next combined map/package test build rather than producing a dedicated APK for this cosmetic adjustment.
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

A cross-platform Python desktop builder now lives under `tools/maps/` and reproduces the physically accepted Eryri Pass 3 inputs:

- Protomaps / OpenStreetMap basemap through native z15;
- Mapterhorn terrain covering full Eryri through z12, the established central area through z15 and the tighter summit/walking area through native z16;
- OS Terrain 50 10 m contours clipped to Eryri and converted to local PMTiles;
- the pinned local glyph PBF used for offline contour labels;
- deterministic SHA-256 and byte-size metadata for every payload.

The builder writes a standalone package and does not require Android tooling. It uses the pinned `go-pmtiles` CLI and standard desktop Python libraries.

## EXPLORE: transport/container shape

For the first import proof, a ZIP containing `manifest.json` plus the independent PMTiles/glyph payloads is acceptable as a **spike transport container only**. The filename/extension and manifest schema are not FINAL product decisions.

The spike manifest identifies:

- schema version;
- package ID and display name;
- geographic bounds;
- payload path, byte count and SHA-256;
- layer type and native zoom ceiling where relevant;
- source/attribution metadata;
- explicit statement that runtime networking is not required.

The Android importer rejects missing, unexpected, path-traversal, oversized or hash-mismatched payloads before replacing the currently installed regional package.

## Android import proof

The isolated `.mapspike` application now uses an explicit Storage Access Framework import flow:

1. app opens without a regional map package and explains that one must be imported;
2. user chooses the package through Android's system document picker;
3. package is validated before installation;
4. installation is staged and atomically replaced into TrailCharter-managed private storage;
5. the same accepted Eryri style renders from imported local files;
6. the package is discoverable again after app restart;
7. a replacement failure restores/preserves the previous package rather than deliberately deleting it first;
8. no broad storage permission is added;
9. no INTERNET/network/location permission is added by the package split;
10. the legacy Run #111 embedded-package copy is deleted only after a replacement package has been fully installed and reopened successfully.

## Run #120 CI evidence

Run #120 is the first CI-verified package-split candidate at isolated map-spike versionCode 16.

- Desktop builder completed from the same Pass 3 source inputs and CI independently reopened and verified the generated package.
- Standalone Eryri package: **104,585,139 bytes**; SHA-256 `6015679159cba649484e127b6ae1677e29f4837d034404c4d382e581983d7416`.
- Package payloads: basemap 5,189,037 bytes; terrain 90,095,950 bytes; contours 9,223,329 bytes; glyph PBF 74,696 bytes; manifest 1,505 bytes.
- Package manifest declares `runtimeNetworkRequired: false` and includes byte counts and SHA-256 values for every payload.
- Package-split APK: **86,870,664 bytes**; SHA-256 `c2da525e8fbd5d994b42a3af5dd34b3cb4a53f3905485ed0f2f09936d766f2a0`.
- Compared with Run #111's 191,421,549-byte embedded APK, the application package is roughly **55% smaller** while preserving the same Eryri data as a separate local artifact.
- CI verifies that no `assets/map_spike/` payload remains inside the APK.
- Unit tests, lint, assemble, package verification, versionCode 16 identity, continuity signing and the privacy baseline all pass.
- The final APK remains free of INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, coarse/fine location and broad external-storage permissions.
- The Run #111 slider/toggle feedback is included: the whole cluster is raised by approximately 2 cm relative to Run #111 while preserving the accepted smaller thumb and vertical control design.

**Physical acceptance remains pending.** Run #120 must prove on the Android 16 device that the package imports through the system picker, the accepted Eryri map renders from the imported package, the package survives restart, the slider position is now right, and the z16 hillshade seam correction remains satisfactory.

## Acceptance evidence for the package split

- APK size falls materially because regional basemap/terrain/contours are no longer embedded. **CI PASS in Run #120.**
- Eryri package imports through the system picker and renders identically enough to the accepted Pass 3 cartography for physical comparison. **Physical pending.**
- The 2 cm upward slider/toggle adjustment is included in this combined build. **CI present; physical pending.**
- Imported package survives process death and normal app restart. **Physical pending.**
- Invalid/tampered package is rejected without damaging the previously installed valid package. **Implementation/CI review present; destructive physical test deferred until basic import proof passes.**
- Final APK remains free of INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, coarse location and fine location permissions. **CI PASS in Run #120.**
- App does not make network requests while opening, importing or rendering a local package. **Architecture/permission boundary present; physical runtime confirmation pending.**
- Package byte sizes and hashes are recorded explicitly. **CI PASS in Run #120.**
- The spike container/manifest remains EXPLORE until physical import evidence exists.

## Routing follows this boundary

The existing `RoutingEngineBoundary` remains authoritative. Once the package split is physically proven, start the previously recorded BRouter-first offline walking-routing comparison using representative Eryri routes. Routing data must be treated as another local package concern rather than being hidden back inside an ever-growing APK.

Valhalla remains the second comparison candidate. Neither becomes production-selected without physical route-quality and integration evidence.
