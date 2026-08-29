# Map cartography Run #105 physical refinement

Status: **AGREE / SPIKE**

This note records physical-device feedback on the CI-verified Pass 3 UI-refinement APK from GitHub Actions Run #105. It supplements `MAP_CARTOGRAPHY_PASS_3.md` and remains isolated in draft PR #13.

## Physical result

Run #105 was physically tested after the earlier Run #93 substantive Pass 3 behaviour had been accepted. The revised right-side vertical Tilt/Zoom control was judged materially more intuitive. The accepted underlying Pass 3 map behaviour, contours, relief and camera interaction are not reopened by this refinement.

## AGREE: next UI refinement

1. Move the vertical slider and its Tilt/Zoom toggle down into the lower-right quarter of the screen rather than centring the control group vertically.
2. Retain safe separation from Android navigation/system insets and keep the toggle directly below the slider.
3. Reduce the visible circular slider thumb slightly. The implementation changes the drawn thumb radius from 18 dp to 15 dp while retaining the larger slider canvas/touch area.
4. Preserve the existing map-aware light/dark control palette and the separate safely inset MapLibre compass.

## AGREE: hillshade tile joins

Physical testing of Run #105 shows faint pale grid-like joins in the relief at the closest zoom. These are terrain/hillshade tile-edge artefacts rather than contour geometry or intentional map boundaries.

The current spike packages genuine Mapterhorn raster-DEM terrain through native z16 in the tight summit/walking test area, but Run #105 allows the camera to z17. MapLibre Native issue #4281 documents a hillshade discontinuity at raster-DEM tile boundaries when the renderer is overzoomed beyond the DEM source `maxzoom`. The proposed upstream structural fix in MapLibre Native PR #4280 was closed without merge, so TrailCharter must not assume the current renderer contains that fix.

For this embedded spike, AGREE to remove the known overzoom condition rather than obscure the joins cosmetically:

- cap map camera and Zoom-slider maximum at z16, matching the highest genuine DEM zoom packaged by Pass 3;
- retain all native z16 terrain detail and existing contours;
- do not represent z17 magnification as additional terrain or path detail;
- physically verify that the visible tile joins disappear or become imperceptible at the native ceiling;
- if seams remain visible at native z16, investigate the DEM neighbour/backfill/render path rather than adding a masking overlay;
- later PC-generated regional packages may extend the useful close-zoom ceiling only where genuine higher-native-resolution terrain/vector data or an upstream renderer fix justifies it.

This keeps the quality-first principle intact: the zoom ceiling is being aligned with the real source resolution because of a renderer artefact, not reduced merely to make the APK smaller.

## Next physical acceptance

The next map-spike APK should be versionCode 15 and must:

- install deterministically over Run #105;
- place the slider/toggle comfortably in the lower-right quarter;
- make the 15 dp thumb visibly less dominant without making the slider harder to operate;
- preserve Tilt/Zoom mode switching and live camera synchronisation;
- preserve compass access, contours, map gestures and accepted Pass 3 cartography;
- remove or materially eliminate the pale hillshade tile-grid joins seen in the Run #105 screenshot;
- remain network/location silent and pass the existing package, signing and privacy checks.

PR #13 remains draft. Physical acceptance of this refinement is required before closing the final embedded-heavyweight Pass 3 comparison and moving heavyweight map-package preparation to the PC pipeline.