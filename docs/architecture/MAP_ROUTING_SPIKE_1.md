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
- MapLibre Compose 0.14 uses the Vulkan-backed Android SDK by default. The first physical-device Map Spike build (`run #55`) crashed immediately on opening the map surface. This is consistent with current upstream MapLibre Vulkan crash reports on some Android devices, including Android 16.
- The controlled retest changes **only the rendering backend** to MapLibre Android OpenGL ES using the documented compatibility setup: exclude `org.maplibre.gl:android-sdk` and add `org.maplibre.gl:android-sdk-opengl:13.0.2`. The PMTiles fixture, map style, activity and privacy configuration are otherwise unchanged.
- CI `run #57` passes with the OpenGL backend, including tests, lint, final-APK privacy permission verification, signing verification, Room schema upload and APK assembly. Physical-device OpenGL retest remains pending.

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

1. **MapLibre remains the leading rendering candidate** because local single-file PMTiles, vector styling, raster imagery and raster-DEM hillshade all fit TrailCharter's modular requirements.
2. Vulkan is **not accepted as the TrailCharter baseline** on current evidence because the first physical-device spike crashed at map startup. OpenGL is now the controlled compatibility retest path.
3. MapLibre's transitive permissions are not a blocker because Android manifest merging allows TrailCharter to remove them, but the final-APK permission check must remain a hard CI guard until individual permissions are deliberately introduced.
4. **BRouter remains the lower-complexity routing candidate at runtime** for an outdoor-first implementation, but its build/distribution path needs a deliberate source-module or package strategy.
5. **Valhalla remains the richer routing candidate** where robust map matching, mixed travel modes and possible future traffic integration carry more weight, with a larger native integration and version-compatibility surface.
6. Map rendering and routing should remain separate interfaces regardless of the eventual engine choices.
7. Aerial imagery is technically straightforward at renderer level but provider licensing, attribution, caching and offline rights are the real constraints.
8. Live traffic can potentially use regional public/open feeds without transmitting continuous device location, but coverage and provider terms require a separate investigation.

## Next evidence required

- Physically retest the identical local-PMTiles spike with the OpenGL backend from CI `run #57`.
- If OpenGL is stable, proceed to representative UK vector data and local raster-DEM hillshade.
- Measure APK/package size impact of the renderer dependency.
- Separately prototype BRouter and valhalla-mobile behind `RoutingEngineBoundary` using representative UK walking routes.
- Compare route quality, waypoint snapping, recalculation, distance/elevation/ETA, memory and calculation time.
- Compare integration/reproducibility burden as well as runtime performance.
- Do not merge an engine choice into production until the physical-device comparison exists.
