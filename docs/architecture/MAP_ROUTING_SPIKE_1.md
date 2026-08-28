# Map / routing spike 1

Status: **EXPLORE / SPIKE**

Purpose: prove that TrailCharter can keep Adventure data, map rendering, offline packages and routing replaceable while preserving the agreed future capabilities. This spike does not select a production engine.

## Evidence from current upstream capabilities

### MapLibre Native / Compose

- Current Android MapLibre supports local PMTiles using `pmtiles://file://...` for vector, raster and raster-DEM sources.
- Raster layers provide a direct technical path for optional aerial/satellite imagery.
- Raster-DEM plus `HillshadeLayer` provides client-side shaded relief on Android.
- The current MapLibre style-spec support table does **not** show 3D terrain extrusion as supported by MapLibre Native Android. Near-term TrailCharter topo expectations should therefore be contours + shaded relief rather than 3D terrain until that changes or another renderer is selected.
- MapLibre Native is BSD-2-Clause; MapLibre Compose is a maintained Compose wrapper published through Maven Central.
- **Important privacy finding:** the MapLibre Native Android library manifest contributes `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` permissions by default. TrailCharter's offline spike explicitly removes all five during manifest merge. The CI spike checks the final APK, not merely TrailCharter's source manifest, so a transitive dependency cannot silently widen the current privacy baseline.
- MapLibre Compose 0.14.0 currently resolves MapLibre Android 13.0.2.
- The first physical Map Spike (`run #55`, Vulkan) crashed when the map surface opened.
- A controlled OpenGL-only backend retest (`run #57`) also crashed, so Vulkan alone is **not** the root cause.
- A staged Compose diagnostic (`run #59`) opened safely before a map was created, but every stage that instantiated `MaplibreMap`, including an engine-only background with no PMTiles or route data, crashed on the physical Android 16 device. This eliminates PMTiles and inline route styling as the immediate startup trigger.
- A deeper native diagnostic (`run #60`) then bypassed MapLibre Compose. Stage 1, `MapLibre.getInstance()` with no `MapView`, **passed physically**. Stage 2, a direct SurfaceView-backed `MapView`, crashed and Android recorded `JAVA CRASH; status/signal=0`.
- Inspection of `run #60` found that its direct test harness was not faithfully ordering `MapView` lifecycle events: it invoked `onCreate`/start/resume before the Android view was guaranteed to be attached. MapLibre Compose's own Android implementation creates the `MapView` first and then forwards lifecycle events through a Lifecycle observer. Therefore the `run #60` Stage 2 crash is **invalid evidence against SurfaceView itself**.
- `run #61` corrects the direct native diagnostic to mirror MapLibre Compose's lifecycle ordering exactly and also persists the full uncaught Java exception stack before Android's normal crash handler runs. Physical retest is pending.

### BRouter

- BRouter accepts start/end plus intermediate via points and routes them on the OSM routing graph.
- Its profiles can correct misplaced via points, which is compatible with interactive magnetic route planning where a dragged/plotted point is snapped back to an appropriate routable network.
- It remains especially credible for offline walking/cycling and elevation-aware routing.
- General map matching of an imported arbitrary trace is not the same strength as Valhalla's explicit map-matching API and must not be assumed.
- Current `1.7.10` routing modules are MIT licensed and published through GitHub Packages rather than ordinary Maven Central. Public TrailCharter CI must therefore not assume a credential-free Maven dependency path.
- A credible alternative is to compile only the required pinned BRouter routing modules from source. That keeps builds reproducible and avoids depending on a private package credential, but adds source-integration maintenance that must be measured in the prototype.

### Valhalla / valhalla-mobile

- Valhalla exposes explicit map matching, including `map_snap`, which can snap an input shape to mapped OSM roads/paths.
- Valhalla costing supports pedestrian, bicycle and driving modes and contains traffic-speed concepts, including current traffic speed.
- `valhalla-mobile` provides an Android library using pre-built downloaded Valhalla tilesets and remained actively released in 2026.
- Its mobile wrapper currently focuses on route generation; additional Valhalla functionality may require wrapper work.
- A current compatibility issue reports that `valhalla-mobile 0.5.1` and the newer Kotlin `valhalla-models 0.2.0` do not interoperate correctly. TrailCharter must pin a known-compatible combination during the prototype rather than taking latest versions independently.

### UK live-traffic data observations

- Live traffic does not automatically require sending the user's GPS position to a commercial navigation provider. TrailCharter can investigate area/corridor data feeds that are fetched independently of user identity/location.
- National Highways exposes public traffic/open-data services. WebTRIS has a JSON API available without registration/API keys for traffic-flow data, while the National Highways Developer Portal provides additional operational road datasets/APIs.
- Transport for London exposes current road disruptions and other traffic feeds through its open-data APIs, although registered/API-key access and usage metrics apply at higher request levels.
- Coverage, freshness and semantics differ by provider, so there is no single UK-wide feed selected yet.
- Any traffic integration remains optional, off by default and behind TrailCharter's explicit network boundary. Provider requests must be inspectable and must not become background location tracking.

## Spike code boundary

This branch introduces engine-neutral contracts and a renderer proof:

- `geo/GeoModels.kt`: coordinates, bounds and route geometry.
- `map/MapContracts.kt`: renderer capabilities and app-managed offline-package boundary.
- `routing/RoutingContracts.kt`: magnetic/direct route requests, travel modes, route estimates, snapped waypoints and a replaceable routing-engine interface.
- The Map Spike uses a tiny deterministic app-managed PMTiles fixture and an entirely local style, with no map/style/tile networking.
- The Map Spike installs beside normal TrailCharter under a separate application ID and launches a dedicated spike Activity.
- CI checks the **final APK permissions** and rejects network/location permissions at the current stage.

Room schema is unchanged. No production renderer/routing engine is selected.

## Current technical interpretation

1. **MapLibre remains under active evaluation, not selected.** Its feature set still fits TrailCharter well, but the physical-device crash must be explained before it can be accepted.
2. Vulkan is not the sole cause because the OpenGL retest also crashed.
3. PMTiles and route styling are not the immediate cause because an engine-only Compose map crashes before either is used.
4. MapLibre Native library initialisation itself works on the physical Android 16 device; the failure occurs later when a map/rendering path is introduced.
5. The `run #60` direct SurfaceView result cannot be used to condemn SurfaceView because the diagnostic lifecycle ordering was incorrect. `run #61` is the authoritative corrected direct-renderer diagnostic.
6. MapLibre's transitive permissions are not a blocker because Android manifest merging allows TrailCharter to remove them, but the final-APK permission check must remain a hard CI guard until individual permissions are deliberately introduced.
7. **BRouter remains the lower-complexity routing candidate at runtime** for an outdoor-first implementation, but its build/distribution path needs a deliberate source-module or package strategy.
8. **Valhalla remains the richer routing candidate** where robust map matching, mixed travel modes and possible future traffic integration carry more weight, with a larger native integration and version-compatibility surface.
9. Map rendering and routing should remain separate interfaces regardless of the eventual engine choices.
10. Aerial imagery is technically straightforward at renderer level but provider licensing, attribution, caching and offline rights are the real constraints.
11. Live traffic can potentially use regional public/open feeds without transmitting continuous device location, but coverage and provider terms require a separate investigation.

## Next evidence required

- Physically run corrected diagnostic `run #61` in order: library init, direct SurfaceView, then direct TextureView only if the preceding stage passes.
- If a corrected direct-map stage crashes, reopen the diagnostic and capture both Android's previous-process record and the persisted Java exception stack before starting another stage.
- If direct native rendering is stable while MapLibre Compose remains unstable, reject the Compose wrapper but retain MapLibre Native as a possible renderer.
- If corrected direct SurfaceView and TextureView both fail, reject MapLibre for the current TrailCharter Android baseline and move the renderer spike to Mapsforge.
- If a stable renderer path is proven, proceed to representative UK vector data and local raster-DEM hillshade.
- Measure APK/package size impact of the renderer dependency.
- Separately prototype BRouter and valhalla-mobile behind `RoutingEngineBoundary` using representative UK walking routes.
- Compare route quality, waypoint snapping, recalculation, distance/elevation/ETA, memory and calculation time.
- Compare integration/reproducibility burden as well as runtime performance.
- Do not merge an engine choice into production until the physical-device comparison exists.
