# Run #120 package-split physical acceptance

Status: **PHYSICAL PASS / CARTOGRAPHY DEFECT OPEN**

Run #120 is the first TrailCharter map-spike build in which the Android APK and the heavyweight Eryri regional map data are separate artifacts.

## Physical result

Physical testing on the Android 16 TrailCharter test device confirmed:

- the package-split APK installs and opens correctly;
- the standalone Eryri package imports successfully through Android's system document picker;
- the imported package renders the expected Eryri basemap, z16 relief, OS Terrain 50 10 m contours, contour labels and paths;
- the package remains usable after installation rather than requiring map data to be embedded in the APK;
- the revised vertical Tilt/Zoom slider/toggle is in the accepted position after being raised approximately 2 cm from Run #111;
- the existing map gestures and separate compass continue to behave as expected.

The package split therefore has **basic physical acceptance** and no longer blocks the BRouter-first routing spike.

## Hillshade tile-edge correction

The screenshot from the physical test still shows faint straight tile-edge lines in the shaded-relief surface at close zoom. Contour lines cross these joins continuously and are not displaced or broken.

This refines the earlier diagnosis:

- Run #105 made the artefact more obvious by allowing raster-DEM overzoom beyond the genuine z16 terrain ceiling.
- Run #111/#120 correctly stop at native z16, which avoids pretending that overzoom provides more terrain detail and reduces the artefact.
- **However, the joins do not disappear completely at native z16.** The remaining faint lines therefore cannot be attributed solely to overzoom. They are consistent with tile-edge sampling/rendering in MapLibre Native's raster-DEM hillshade path.

Status: **OPEN, NON-BLOCKING**.

Do not mask the joins with fake detail or re-enable z17 overzoom. Investigate renderer/style/data mitigations separately, including whether a precomputed hillshade layer or another terrain-rendering strategy materially improves the result without harming offline size, quality or licensing. Do not reopen the accepted contour, path or map-package work merely to chase this artefact.

## Package-split acceptance position

- Separate APK and regional package: **PASS**.
- Explicit local package import: **PASS**.
- Imported Eryri rendering: **PASS**.
- Slider/toggle physical position: **PASS**.
- z16 hillshade seams: **minor defect remains**.
- Package container/manifest format: remains **EXPLORE**, not FINAL.
- Privacy/network baseline: unchanged; Run #120 CI remains authoritative for forbidden-permission checks.

## Next implementation gate

Proceed to the BRouter-first offline walking-routing prototype behind the existing `RoutingEngineBoundary`.

Routing data must follow the package separation principle proven here: routing graph/profile data is a local, independently manageable package concern and must not be hidden back inside the Android APK.

BRouter remains an EXPLORE candidate until physical route-quality, snapping, recalculation, elevation, memory and calculation-time evidence exists. Valhalla remains the second comparison candidate.
