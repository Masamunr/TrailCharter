# Map spike pass 2

Status: **EXPLORE / SPIKE**

This file records evidence and next technical probes for draft PR #13. It does not select a production map, routing, contour or 3D engine and does not authorise merging the spike into `main`.

## Fixed constraints

The existing TrailCharter privacy and offline decisions remain authoritative. In particular, the map spike must not introduce `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, coarse location or fine location merely to satisfy a renderer. MapLibre remains forced disconnected before map creation. CI must continue checking the final merged APK permission set.

Map rendering and routing remain separate replaceable boundaries. True 3D remains an agreed capability with its implementation still EXPLORE until physical evidence exists.

## Cartography pass 2 implementation

Pass 2 raised the controlled Eryri prototype to:

- Protomaps / OSM vector basemap extracted through native z15.
- Mapterhorn terrain composed from the global z0-z12 archive plus regional high-resolution terrain above z12.
- One merged local terrain PMTiles package consumed by MapLibre.
- Local MapLibre style source ceilings explicitly set to z15.
- Map camera allowed to overzoom beyond the native package ceiling for inspection.
- One-tap view presets for approximately 0°, 30° and 50° pitch. This was a usability spike, not a final control design.

The app remains network-silent at runtime. The map packages are fetched only by CI at build time and embedded in the spike APK.

## Run 75 CI evidence

GitHub Actions run **#75** completed successfully for commit `ed10e72eb2b18d3f6d7bc37226084c74a9ee0c1d`.

CI passed:

- offline map asset preparation;
- unit tests, lint and debug assembly;
- embedded PMTiles storage verification;
- forbidden network/location permission verification;
- continuity signing certificate verification; and
- artifact upload.

Artifact inspection after CI:

| Item | Bytes | Approx. MiB |
| --- | ---: | ---: |
| APK | 378,696,616 | 361.2 |
| Eryri vector PMTiles | 5,189,037 | 4.9 |
| Eryri terrain PMTiles | 286,751,296 | 273.5 |
| GitHub artifact ZIP | 323,480,431 | 308.5 |

The binary PMTiles headers in the APK report z0-z15 for both the vector and terrain archives, with bounds `-4.22,52.97,-3.95,53.18`.

### Interpretation

The full-region z15 terrain experiment established that the visual-data path worked, but its roughly 287 MB terrain archive and 361 MiB APK were not a practical sideload/install candidate. Run #75 is therefore superseded for physical testing.

## Run 79 corrective packaging and physical evidence

Run **#79** replaced the full-region high-zoom terrain package with:

- full Eryri z0-z12 terrain;
- a controlled central Yr Wyddfa z13-z15 terrain area;
- the same native z15 Protomaps vector basemap;
- isolated `com.masamunr.trailcharter.mapspike` identity at versionCode 12; and
- hard CI limits of <=100 MiB for the merged terrain package and <=190 MiB for the final spike APK.

Measured Run #79 package sizes:

| Item | Bytes | Approx. MiB |
| --- | ---: | ---: |
| APK | 164,819,900 | 157.2 |
| Terrain PMTiles | 72,874,578 | 69.5 |

Run #79 passed unit tests, lint, assembly, embedded-uncompressed PMTiles verification, install-identity/version checks, continuity-signing verification and the network/location-silent permission baseline.

### Physical acceptance, 28/08/2026

Run #79 downloaded, installed and ran successfully on the physical Android 16 Motorola Edge 50 Pro. The user reported that the build works well overall. This is the physical acceptance point for Cartography Pass 2.

Accepted evidence carried forward:

1. The separate `.mapspike` package installs and runs beside normal TrailCharter.
2. The corrective package size is practical enough for physical testing.
3. The offline Eryri map and local hillshade render successfully.
4. The previous water/cartography corrections remain acceptable.
5. Camera pitch controls function physically.
6. No runtime permission/connectivity regression was reported.

Physical acceptance does **not** make the current package shape or pitch-button UI final. The user specifically requested finer close-up resolution and replacement of the preset-button stack with a slider-based camera control. Those refinements are recorded in `docs/architecture/MAP_CARTOGRAPHY_PASS_3.md`.

## True 3D terrain implementation: EXPLORE

TrailCharter must not label a tilted 2D hillshade map as 3D. A true 3D mode must displace/render terrain from elevation data.

### Candidate A: Filament + TrailCharter terrain mesh

**Current preferred first controlled Android experiment.**

Filament is a current real-time rendering engine with Java/JNI Android APIs and OpenGL ES / Vulkan support. It is Apache-2.0 licensed. It is a renderer rather than a GIS engine, which is useful for keeping the 3D implementation behind a dedicated TrailCharter boundary but means TrailCharter must provide geospatial tiling, DEM decoding, terrain mesh generation, LOD and map-texture draping.

Proposed first probe:

- retain MapLibre as the proven 2D/topographic renderer;
- add a separate `Terrain3dRenderer` boundary;
- feed it a small local Eryri DEM tile set only;
- generate a deterministic terrain mesh from local elevation samples;
- render the mesh in Filament with no network permission and no network accessor;
- initially use simple local colouring or a local pre-rendered texture rather than attempting full cartographic parity;
- measure added APK/library size, mesh-generation time, RAM, GPU behaviour, frame responsiveness and lifecycle stability on the physical device.

This proves or disproves the renderer path without committing TrailCharter to a full 3D tiling stack.

### Candidate B: Cesium Native + Android renderer integration

Cesium Native is Apache-2.0 and provides 3D geospatial tiling, glTF support and quantized-mesh terrain support. It is not a turnkey Android UI/map SDK. Its integration model requires the host application to provide task processing, asset access and renderer-resource preparation.

A fully local `IAssetAccessor` is technically compatible with TrailCharter's offline model, and Cesium Native could later provide a stronger LOD/streaming foundation than a bespoke terrain tiler. The cost is materially higher integration complexity, likely NDK/C++ work, and the need to pair it with a renderer such as Filament or another graphics layer.

Recommendation for the spike sequence: do not start here unless the simpler Filament terrain-mesh probe proves that 3D rendering is viable on the target Android device and we then need more scalable terrain LOD/3D Tiles behaviour.

### Deprioritised candidates

- **NASA WorldWind Android:** genuine 3D globe/elevation capability, but the Android SDK/release guidance visible today is old and the NASA Open Source Agreement is less straightforward for this project than Apache-2.0 alternatives. Keep as reference, not the first spike.
- **WhirlyGlobe-Maply:** Android-capable and Apache-2.0, but the project states that it is not being actively maintained. Do not base a new TrailCharter production path on it without a compelling reversal in project health.

No 3D production engine is selected by this document.

## Contours: offline UK strategy

Contours should be real vector data, not invented from the MapLibre style.

### Great Britain

The preferred source to prototype is **OS Terrain 50**. It is OS OpenData and provides 10 m interval contours, including vector-friendly formats. For TrailCharter this is preferable to runtime contour generation because the linework can be prepared once, clipped to the package region and encoded as a local vector layer/PMTiles archive.

Controlled pipeline to test:

1. obtain OS Terrain 50 contour data under its applicable open-data licence;
2. clip to the Eryri package boundary at build/package preparation time;
3. retain elevation as an attribute;
4. convert to tiled vector data and package locally, preferably PMTiles so it fits the existing offline-package model;
5. style index/intermediate contours separately and test label density at walking zooms;
6. measure package-size cost before widening coverage.

### Northern Ireland

OS Terrain 50 covers Great Britain, not Northern Ireland. OSNI/Land & Property Services publishes open 10 m and 50 m DTM data for Northern Ireland under open-data terms. A UK release therefore needs either:

- OS Terrain 50 contours for Great Britain plus deterministic build-time contour generation from OSNI DTM for Northern Ireland; or
- a single UK-wide elevation source from which contours are generated consistently at package-build time.

The first option is the preferred UK-specific experiment because it uses authoritative open national mapping/elevation sources while keeping contour generation off the phone.

Runtime contour calculation remains unnecessary for the current design.

## Routing gate after this pass

Cartography Pass 2 now has physical acceptance through Run #79. The next agreed cartography refinement is Pass 3, covering slider camera controls and a controlled genuine close-up terrain-resolution increase. Routing comparison remains gated until that pass has physical evidence, so map interaction/data packaging are not changed at the same time as the routing engine comparison.

After Pass 3 physical acceptance, prototype routing behind the existing routing boundary:

1. BRouter first as the lower-complexity outdoor/offline magnetic-routing candidate.
2. Valhalla second as the richer map-matching/multimodal candidate with higher integration cost.

Required evidence remains snap-to-road/path/trail behaviour, recalculation when points move, outdoor walking quality, distance/time output and preferably elevation output. Neither engine becomes production-selected on CI evidence alone.

## External technical references checked 28/08/2026

- Filament repository/readme and Android API guidance: https://github.com/google/filament
- Cesium Native repository and rendering integration guidance: https://github.com/CesiumGS/cesium-native and https://cesium.com/learn/cesium-native/ref-doc/rendering-3d-tiles.html
- Mapterhorn data access: https://mapterhorn.com/data-access/
- Protomaps basemap downloads: https://docs.protomaps.com/basemaps/downloads
- OS Terrain 50 product information: https://www.ordnancesurvey.co.uk/products/os-terrain-50
- OS OpenData catalogue: https://www.ordnancesurvey.co.uk/products/open-data
- OSNI 10 m / 50 m DTM information: https://www.nidirect.gov.uk/articles/10m-digital-terrain-model-height-data and https://www.nidirect.gov.uk/articles/50m-digital-terrain-model-height-data
