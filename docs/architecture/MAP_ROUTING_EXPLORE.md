# TrailCharter UK Offline Map and Routing Investigation

Status: **AGREE WORKSTREAM / EXPLORE ARCHITECTURE**

This document records the agreed next technical workstream after the first playable Adventure/Stage alpha. It does **not** select or lock a map renderer, routing engine, package format or distribution provider.

## Agreed investigation direction

- Refine the **Stage / Place** product model in parallel with the technical mapping investigation.
- TrailCharter remains **UK-only** for the current release geography.
- Core planning, saved Adventures and downloaded map/routing data must remain usable without a TrailCharter-owned server.
- Offline map rendering and on-device routing are first-class requirements rather than afterthoughts.
- A map/routing engine must not be selected until licensing, Android integration, offline-data packaging, storage cost, battery/performance, maintainability and future freedom have been examined.
- Prefer replaceable boundaries between **map rendering**, **routing**, **map/routing package management**, and the TrailCharter Adventure/Stage/Place model.
- The previously recorded date-range picker header-alignment issue remains a minor next-pass UI item and is not a standalone build reason.

## Product capability requirements

Status: **AGREE CAPABILITY / EXPLORE IMPLEMENTATION**

The architecture selected for TrailCharter must preserve a credible implementation path for the following mapping capabilities. A technical choice that makes these impractical without a strong reason should be rejected.

### Magnetic route planning / route snapping

- Interactive route planning should support a **magnetic** mode in which user-selected route points snap to known routable paths/roads/trails rather than requiring freehand geometry.
- Snapping should be driven by the routing graph/profile so walking, cycling and later driving modes can follow appropriate known networks.
- The user must remain able to alter the proposed route and, where justified, use manual/free geometry rather than being trapped by an incorrect automatic route.
- Route snapping and recalculation should work offline once the required routing package is installed.

This is a **required route-planning capability**, not merely a visual enhancement.

### Topographic mapping and relief

- TrailCharter should support a useful **topographic map presentation** for outdoor planning.
- The selected renderer/package architecture must be capable of showing terrain-oriented styling, contour/elevation information and **hillshade/shaded relief where technically and legally practical**.
- Terrain/elevation data may be packaged separately from the base vector map so users are not forced to download it when unnecessary.
- Exact DEM/contour sources, resolution, storage cost and update process remain EXPLORE.

### Aerial / satellite imagery

- The map architecture must not prevent TrailCharter from offering an optional **aerial imagery layer** later.
- Aerial imagery is expected to have different licensing, attribution, storage and network constraints from the OSM-derived base map and should therefore remain an interchangeable map layer rather than being baked into core map data.
- Online imagery, user-downloaded offline imagery and provider-specific limitations must be investigated before a source is selected.
- Aerial imagery must never be required for core/offline TrailCharter use.

### Live traffic

Status: **EXPLORE / PRIVACY-SENSITIVE NETWORK FEATURE**

- Live traffic is worth investigating for road/travel stages, but it is not part of the offline core.
- Any live-traffic implementation would inherently require current external data and therefore network access.
- It must be **off by default**, clearly user-initiated or explicitly enabled, and isolated behind TrailCharter's network boundary.
- Merely opening TrailCharter or viewing an offline map must not contact a traffic provider.
- No TrailCharter-owned server should be required; any external traffic source must be replaceable and its privacy/licensing terms reviewed before adoption.
- The app should disclose when live traffic is active and what external dependency is being contacted.
- Traffic data must not be allowed to become a justification for background tracking, analytics or silent location uploads.

## Current candidate assessment

### MapLibre Native

Current position: **strong renderer candidate; continue investigation**.

- Open-source native map renderer with Android support.
- BSD-2-Clause core licensing is compatible with TrailCharter's open-source model.
- Supports offline regions and local map resources.
- Current Android releases support local `pmtiles://file://...` sources, allowing a single local PMTiles archive to be rendered without a live tile server.
- MapLibre is a renderer, not a routing engine. TrailCharter would retain responsibility for map style, offline package generation/distribution, attribution and any routing integration.

Potential advantage: clean separation between TrailCharter's UI/data model and the rendering engine, with strong control over TrailCharter's own visual identity.

Potential concern: TrailCharter must solve its own UK vector-tile, terrain/contour and package-update pipeline rather than inheriting a complete offline-navigation stack.

### Mapsforge

Current position: **credible fallback / comparison candidate**.

- Mature Android/Java offline vector-map library.
- Supports compact Mapsforge map files, custom render themes and offline hillshading.
- Active project with an Android integration path.
- LGPLv3 with the project's stated Android redistribution simplification.

Potential advantage: purpose-built offline map-file workflow and a long history of use in Android navigation applications.

Potential concern: less aligned than MapLibre with modern vector-tile styling/ecosystem and still provides no routing engine by itself.

### Organic Maps

Current position: **useful reference and possible companion integration; not currently preferred as TrailCharter's embedded primary architecture**.

- Organic Maps is an excellent privacy/offline reference implementation with walking, cycling and car routing, elevation features, search and GPX support.
- The project now provides an Android API, but that API is primarily a thin wrapper around deep links/intents into the separately installed Organic Maps application rather than a general embedded map SDK.
- Organic Maps application code is Apache-2.0, but its binary `.mwm` map data has separate attribution/branding conditions and derivative/white-label use requires care.
- Embedding/forking the complete Organic Maps application would also create a large C++/Android maintenance surface and risk TrailCharter becoming coupled to another application's UX and data format.

Possible future use: optional user-initiated handoff to an installed Organic Maps app, while TrailCharter retains its own internal map architecture.

### BRouter

Current position: **strong on-device routing candidate; continue investigation and benchmark**.

- MIT-licensed configurable OSM offline router implemented in Java/Android.
- Explicitly supports offline routing and elevation-aware profiles.
- Routing data is stored in efficient `rd5` 5°×5° segment files and can be generated from OSM/Geofabrik extracts.
- Can expose an Android routing service and its repository is split into routing/core/map-access modules.
- Particularly established for walking/cycling use cases, with configurable profiles.

Current UK storage observation from BRouter's published segment set (28/08/2026): the six 5° segments needed to span the UK longitude/latitude envelope are approximately 293 MB in total. This is routing data only, separate from the visual map package.

Potential advantage: small, proven Android/offline routing footprint relative to a full navigation stack.

Potential concern: segment granularity is coarse and full multi-modal/vehicle ambitions may eventually fit Valhalla better. Profiles and routing semantics need physical-device testing for TrailCharter's hiking/expedition use cases.

### Valhalla / valhalla-mobile

Current position: **strong alternative routing candidate; continue investigation and benchmark against BRouter**.

- Valhalla is a liberal open-source, tiled routing engine designed for regional extracts, low-memory/offline use and dynamic costing.
- Supports pedestrian, bicycle, driving and richer multi-modal concepts.
- A current `valhalla-mobile` project publishes Android artifacts that route against pre-built Valhalla tilesets; its current release line remained active in 2026.
- Data preprocessing is expected to occur off-device; the phone consumes the prepared tile set.

Potential advantage: richer long-term mixed-mode routing model and small-tile architecture.

Potential concern: native C++/NDK/JNI integration and route-data generation are substantially more complex than BRouter. Package size and performance on the TrailCharter Android baseline need measured evidence, not assumption.

### GraphHopper

Current position: **not preferred for the offline Android core**.

- Strong Apache-2.0 routing engine and Java library.
- Current GraphHopper documentation states that offline Android routing is no longer officially supported; the historical Android demo ended at GraphHopper 1.0.

TrailCharter should not choose an explicitly unsupported mobile path when maintained alternatives exist.

## Map-data/package observations

OpenStreetMap remains the agreed base data source. Public use requires visible OSM attribution and ODbL information.

Geofabrik currently publishes both raw regional OSM extracts and experimental Shortbread vector-tile packages usable by MVT-capable renderers such as MapLibre. Current 27/08/2026 Shortbread MBTiles sizes illustrate the scale of full-country offline rendering data:

- England: about **1.4 GB**
- Scotland: about **423 MB**
- Wales: about **197 MB**
- Ireland and Northern Ireland combined: about **354 MB** (not directly suitable as TrailCharter's UK-only Northern Ireland package without further processing)

This strongly suggests that TrailCharter should investigate **regional/on-demand packages** rather than requiring every user to download the whole UK visual map dataset.

Shortbread is deliberately a lean general-purpose schema and does not represent the full breadth of OSM tagging. TrailCharter therefore needs to verify that hiking paths, route information and other expedition-relevant features are sufficient for the visual map, or generate a TrailCharter-specific schema/package where justified.

Terrain, contour lines and hillshade remain a separate package/data question and are not solved merely by selecting a base-map renderer.

## Preliminary architecture hypothesis

Status: **EXPLORE, not AGREE**.

The most promising modular direction currently looks like:

1. **TrailCharter product layer**: Adventure / Stage / Place / Route remain TrailCharter-owned data models.
2. **Map renderer boundary**: likely MapLibre Native, with Mapsforge retained as a comparison/fallback until tested.
3. **Offline visual packages**: app-managed regional vector packages, likely a single-file format such as PMTiles if the toolchain proves suitable.
4. **Routing boundary**: benchmark BRouter and valhalla-mobile on a physical Android device using representative UK walking routes before choosing.
5. **Routing packages**: preprocessed, user-downloadable regional data; routing remains fully on-device once packages are installed.
6. **Optional map layers**: terrain/hillshade and aerial imagery remain separate from the base map package so they can carry different data sources, licences and download behaviour.
7. **Optional live data layer**: live traffic, if ever adopted, remains network-dependent, isolated and explicitly user-controlled.
8. **No owned runtime backend**: any package hosting/update mechanism must be static/replaceable and must not become a required TrailCharter application server.

This architecture deliberately avoids binding map display, routing logic and TrailCharter's Adventure model into one vendor/project stack.

## Stage / Place model questions to resolve alongside the benchmark

Status: **EXPLORE**.

- `Place`: reusable user-defined physical location with a name and optional coordinates; roles such as start/end/accommodation/parking/water/resupply remain contextual rather than separate coordinate objects.
- `Stage`: optional date plus optional start Place and end Place.
- `Route`: shared route geometry associated with a Stage and/or Adventure rather than copied into multiple screens.
- Derived distance, ascent/descent and expected duration should come from the selected route/profile where available and remain user-understandable/editable; exact persistence/caching rules remain unresolved.
- A Stage must remain valid without a route or mapped Places so simple Adventures do not become map-dependent.

## Required evidence before engine selection

- Build/integration feasibility with the TrailCharter Android toolchain and minSdk 28.
- Representative UK walking route quality, including public rights of way/trails and access-sensitive cases where OSM data permits.
- **Magnetic route-planning quality:** route-point snapping, recalculation, intermediate via points and manual override behaviour.
- Route calculation time and memory on physical Android hardware.
- Battery behaviour during repeated route planning.
- Offline package sizes for realistic UK regions.
- Boundary behaviour when a route crosses two downloaded packages.
- Route distance, elevation and ETA capabilities.
- Topographic styling, contours/terrain and hillshade feasibility, quality and storage cost.
- Ability to add an aerial-imagery source later without replacing the renderer/core map architecture.
- GPX import/export fit.
- Licensing/attribution obligations for software, OSM-derived data, terrain data and any optional aerial/traffic provider.
- Privacy/network behaviour for any optional live traffic source.
- Update/distribution design that does not require a TrailCharter-owned server.

## Authoritative research references

- Organic Maps Android API: https://github.com/organicmaps/api-android
- Organic Maps data licence: https://github.com/organicmaps/organicmaps/blob/master/DATA_LICENSE.txt
- MapLibre Native Android offline API: https://maplibre.org/maplibre-native/android/api/
- Mapsforge: https://github.com/mapsforge/mapsforge
- BRouter: https://github.com/abrensch/brouter
- Valhalla: https://github.com/valhalla/valhalla
- valhalla-mobile: https://github.com/Rallista/valhalla-mobile
- GraphHopper: https://github.com/graphhopper/graphhopper
- OpenStreetMap copyright/licensing: https://www.openstreetmap.org/copyright
- OSMF attribution guideline: https://osmfoundation.org/wiki/Licence/Attribution_Guidelines
- Geofabrik UK extracts: https://download.geofabrik.de/europe/united-kingdom/
- Shortbread vector-tile schema: https://shortbread-tiles.org/
