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
- MapLibre offline rendering is now physically proven on the Android 16 test device.
- Cartography Pass 3 is physically accepted for the spike: local vector map, native-z16 relief, OS Terrain 50 contours/labels and the continuous Tilt/Zoom control work on-device.
- Run #120 physically proves that the same map can be supplied as an independently imported local package rather than embedded in the APK.
- A minor rendering defect remains: faint raster-DEM hillshade tile-edge joins are still visible at close zoom even at native z16. Contours cross them continuously. This is non-blocking for routing work and remains a renderer/cartography investigation item.

### BRouter

- BRouter accepts start/end plus intermediate via points and routes them on the OSM routing graph.
- Its profiles can correct misplaced via points, which is compatible with interactive magnetic route planning where a dragged/plotted point is snapped back to an appropriate routable network.
- It remains especially credible for offline walking/cycling and elevation-aware routing.
- General map matching of an imported arbitrary trace is not the same strength as Valhalla's explicit map-matching API and must not be assumed.
- Current upstream release remains **v1.7.10** (released July 2026).
- The routing implementation is split across Java modules including `brouter-core`, `brouter-mapaccess`, `brouter-util`, `brouter-expressions` and `brouter-codec`; `brouter-core` depends on those four supporting modules.
- This modular structure makes an in-process source integration technically plausible without embedding the full BRouter Android application, but the TrailCharter prototype must measure build maintenance and APK impact rather than assuming it is cheap.
- Published package availability must not become a credentialed or opaque build dependency for the public TrailCharter repository. A pinned-source or otherwise reproducible integration path remains preferred for the spike.

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
- The Map Spike now consumes an explicitly imported standalone Eryri package from app-private storage rather than embedding regional map data in the APK.
- The Map Spike installs beside normal TrailCharter under a separate application ID and launches a dedicated spike Activity.
- CI checks the **final APK permissions** and rejects network/location permissions at the current stage.

Room schema is unchanged. No production renderer/routing engine is selected.

## Current technical interpretation

1. **MapLibre is physically viable for the current offline topo spike, but still not production-selected.**
2. The independent package boundary is physically proven and can now be reused for routing-data experiments.
3. Faint native-z16 raster-DEM hillshade joins remain a known non-blocking rendering defect; do not mislabel them as contour or package corruption.
4. MapLibre's transitive permissions are not a blocker because TrailCharter removes them during manifest merge, but the final-APK permission check remains a hard CI guard until individual permissions are deliberately introduced.
5. **BRouter remains the lower-complexity routing candidate at runtime** for an outdoor-first implementation, but its build/distribution path needs a deliberate reproducible source-module or package strategy.
6. **Valhalla remains the richer routing candidate** where robust map matching, mixed travel modes and possible future traffic integration carry more weight, with a larger native integration and version-compatibility surface.
7. Map rendering and routing remain separate interfaces regardless of eventual engine choices.
8. Aerial imagery remains technically straightforward at renderer level but provider licensing, attribution, caching and offline rights are the real constraints.
9. Live traffic can potentially use regional public/open feeds without transmitting continuous device location, but coverage and provider terms require a separate investigation.

## BRouter physical evidence

Run #136 / versionCode 17 established the first physical routing result:

- the independently imported BRouter WALK package loaded successfully on-device;
- the fixed Pen-y-Pass → Pyg Track via → Yr Wyddfa test completed offline;
- reported route: **5.96 km, +764 m / -129 m, 114 min**;
- this is a **PASS for basic in-process offline BRouter calculation**;
- visual route-quality acceptance remains open because the original solid route overlay obscured the mapped path beneath it;
- a separate Android document-picker MIME filtering issue was identified and corrected in Run #137 by allowing all files to be visible while retaining TrailCharter manifest/hash validation.

### AGREE: route inspectability refinement

For the next physical BRouter pass:

- keep the calculated route visible over the accepted topo map;
- add **adjustable route opacity from 10% to 100%**;
- default to approximately **55% opacity** so the route remains clear while the underlying mapped path can still be inspected;
- update opacity live without recalculating the route;
- do not treat this spike control as final production route-planning UI;
- use the adjustable overlay to judge whether BRouter follows the established mapped walking route and to expose any snapping or geometry errors.

## Next evidence required: BRouter-first slice

The package-split gate has now passed physically. Proceed with BRouter first, without changing the production app or selecting an engine.

First implementation slice:

1. pin BRouter v1.7.10 for the prototype;
2. integrate only the minimum in-process routing modules needed for route calculation rather than the full BRouter app UI;
3. keep the integration behind `RoutingEngineBoundary`;
4. add an independently managed local routing-data/profile location rather than embedding UK routing data in the APK;
5. start with **WALK** only and one representative Eryri routing-data segment/profile;
6. render the returned route geometry over the already accepted imported Eryri map;
7. expose start, end and at least one intermediate/via point in the spike so magnetic waypoint snapping can be observed physically;
8. report route distance, calculation time and any available ascent/descent/ETA data;
9. preserve the existing no-network/no-location-permission CI baseline for this offline prototype;
10. measure APK delta, routing-package size, peak memory and repeated calculation time on the physical Android device.

Do not add bicycle/drive, Valhalla, traffic or production Adventure persistence to the first BRouter slice. One engine, one travel mode, one representative region and measurable evidence are enough. Humanity has suffered adequately from prototypes that attempted the whole product at once.

## Required physical comparison before engine selection

For BRouter, then Valhalla:

- representative Eryri walking route quality;
- start/end snapping and intermediate via-point behaviour;
- recalculation after moving a waypoint;
- distance and elevation/ETA output;
- calculation time and peak memory;
- routing-data storage size;
- boundary behaviour when required data is absent;
- integration/reproducibility burden;
- privacy/network behaviour;
- APK impact.

No routing engine becomes production-selected until this evidence exists.
