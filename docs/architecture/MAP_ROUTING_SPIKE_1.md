# Map / routing spike 1

Status: **EXPLORE / SPIKE**

Purpose: prove that TrailCharter can keep Adventure data, map rendering, offline packages and routing replaceable while preserving the agreed future capabilities. This spike does not select a production engine.

## Evidence from current upstream capabilities

### MapLibre Native

- Current Android MapLibre supports local PMTiles using `pmtiles://file://...` for vector, raster and raster-DEM sources.
- Raster layers provide a direct technical path for optional aerial/satellite imagery.
- Raster-DEM plus `HillshadeLayer` provides client-side shaded relief on Android.
- The current MapLibre style-spec support table does **not** show 3D terrain extrusion as supported by MapLibre Native Android. Near-term TrailCharter topo expectations should therefore be contours + shaded relief rather than 3D terrain until that changes or another renderer is selected.
- Renderer licensing remains BSD-2-Clause.

### BRouter

- BRouter accepts start/end plus intermediate via points and routes them on the OSM routing graph.
- Its profiles can correct misplaced via points, which is compatible with interactive magnetic route planning where a dragged/plotted point is snapped back to an appropriate routable network.
- It remains especially credible for offline walking/cycling and elevation-aware routing.
- General map matching of an imported arbitrary trace is not the same strength as Valhalla's explicit map-matching API and must not be assumed.

### Valhalla / valhalla-mobile

- Valhalla exposes explicit map matching, including `map_snap`, which can snap an input shape to mapped OSM roads/paths.
- Valhalla costing supports pedestrian, bicycle and driving modes and contains traffic-speed concepts, including current traffic speed.
- `valhalla-mobile` provides an Android library using pre-built downloaded Valhalla tilesets and remained actively released in 2026.
- Its mobile wrapper currently focuses on route generation; additional Valhalla functionality may require wrapper work.

## Spike code boundary

This branch introduces engine-neutral contracts only:

- `geo/GeoModels.kt`: coordinates, bounds and route geometry.
- `map/MapContracts.kt`: renderer capabilities and app-managed offline-package boundary.
- `routing/RoutingContracts.kt`: magnetic/direct route requests, travel modes, route estimates, snapped waypoints and a replaceable routing-engine interface.

No map/routing dependency is added. No Room schema is changed. No network permission is added. No production architecture is selected.

## Current technical interpretation

1. **MapLibre remains the leading rendering candidate** because local single-file PMTiles, vector styling, raster imagery and raster-DEM hillshade all fit TrailCharter's modular requirements.
2. **BRouter remains the lower-complexity routing candidate** for an outdoor-first implementation.
3. **Valhalla remains the richer routing candidate** where robust map matching, mixed travel modes and possible future traffic integration carry more weight.
4. Map rendering and routing should remain separate interfaces regardless of the eventual engine choices.
5. Aerial imagery is technically straightforward at renderer level but provider licensing, attribution, caching and offline rights are the real constraints.
6. Live traffic is technically compatible with a Valhalla-style route cost model, but the traffic-feed source is a separate external-data/privacy problem. It remains optional, off by default and outside the offline core.

## Next evidence required

- Compile the engine-neutral contracts on the current Android baseline.
- Add a MapLibre-only rendering proof without enabling general networking: load a small local test package from app-managed storage and display a route-neutral map.
- Test local raster-DEM hillshade in that proof.
- Separately prototype BRouter and valhalla-mobile behind `RoutingEngineBoundary` using representative UK walking routes.
- Compare route quality, waypoint snapping, recalculation, distance/elevation/ETA, memory and calculation time.
- Do not merge an engine choice into production until the physical-device comparison exists.
