package com.masamunr.trailcharter.map

import android.content.Context
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val MIN_CAMERA_ZOOM = 9f
// Pass 3 contains genuine Mapterhorn DEM through z16 only. Staying at the native DEM ceiling also
// avoids MapLibre Native's known hillshade tile-edge seam when raster-dem data is overzoomed.
private const val MAX_CAMERA_ZOOM = 16f
private const val MAX_CAMERA_TILT = 60f

/**
 * Cartography pass 3 renderer using an explicitly imported regional package.
 *
 * Heavy cartographic preparation now happens outside the APK. The Android spike imports a package
 * chosen through the system picker, validates it, and renders only app-private local files.
 */
@Composable
internal fun OfflineUkMapPass3Screen() {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val scope = rememberCoroutineScope()
    var packages by remember(applicationContext) {
        mutableStateOf(loadInstalledEryriMapPackage(applicationContext))
    }
    var importing by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            importing = true
            importError = null
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        importEryriMapPackage(applicationContext, uri)
                    }
                }.onSuccess { installed ->
                    packages = installed
                    importing = false
                }.onFailure { error ->
                    importing = false
                    importError = "${error::class.java.simpleName}: ${error.message.orEmpty()}"
                }
            }
        }
    }

    val installed = packages
    if (installed == null) {
        Pass3MapImportScreen(
            importing = importing,
            error = importError,
            onImport = {
                importLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/x-zip-compressed",
                        "application/octet-stream",
                    ),
                )
            },
        )
    } else {
        OfflineEryriPass3Map(packages = installed)
    }
}

@Composable
private fun Pass3MapImportScreen(
    importing: Boolean,
    error: String?,
    onImport: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Eryri offline map package", style = MaterialTheme.typography.headlineSmall)
            Text(
                "This spike now keeps the regional map outside the APK. Choose the Eryri test package; TrailCharter will validate it and copy it into private local storage.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Import and rendering remain local. No broad storage, network or location permission is required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!error.isNullOrBlank()) {
                Text(
                    "Import failed: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onImport,
                enabled = !importing,
            ) {
                Text(if (importing) "Importing and validating…" else "Choose map package")
            }
        }
    }
}

@Composable
private fun OfflineEryriPass3Map(packages: InstalledOfflineMapPackage) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var currentMapView by remember { mutableStateOf<MapView?>(null) }
    var currentMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var status by remember { mutableStateOf("Starting offline renderer…") }
    var controlMode by remember { mutableStateOf(Pass3CameraControl.TILT) }
    var liveTilt by remember { mutableStateOf(0f) }
    var liveZoom by remember { mutableStateOf(11.4f) }

    DisposableEffect(lifecycle, currentMapView) {
        val mapView = currentMapView
        if (mapView == null) return@DisposableEffect onDispose { }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                Lifecycle.Event.ON_ANY -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    DisposableEffect(currentMap) {
        val map = currentMap ?: return@DisposableEffect onDispose { }
        fun syncCamera() {
            liveTilt = map.cameraPosition.tilt.toFloat().coerceIn(0f, MAX_CAMERA_TILT)
            liveZoom = map.cameraPosition.zoom.toFloat().coerceIn(MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM)
        }

        syncCamera()
        val listener = MapLibreMap.OnCameraMoveListener { syncCamera() }
        map.addOnCameraMoveListener(listener)
        onDispose { map.removeOnCameraMoveListener(listener) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                initialisePass3OfflineMapLibre(viewContext)
                MapView(
                    viewContext,
                    MapLibreMapOptions.createFromAttributes(viewContext)
                        .foregroundLoadColor(Color.rgb(239, 235, 226))
                        .textureMode(false)
                        .maxPitchPreference(MAX_CAMERA_TILT.toDouble())
                        .maxZoomPreference(MAX_CAMERA_ZOOM.toDouble()),
                ).also { mapView ->
                    currentMapView = mapView
                    status = "Loading imported map + relief + contours…"
                    mapView.getMapAsync { map ->
                        currentMap = map
                        val density = viewContext.resources.displayMetrics.density
                        map.uiSettings.apply {
                            setCompassEnabled(true)
                            setCompassGravity(android.view.Gravity.TOP or android.view.Gravity.END)
                            setCompassMargins(
                                0,
                                (56f * density).roundToInt(),
                                (14f * density).roundToInt(),
                                0,
                            )
                            setCompassFadeFacingNorth(false)
                        }
                        status = "Renderer ready; loading local style…"
                        map.setStyle(
                            Style.Builder().fromJson(pass3OfflineEryriStyle(packages)),
                        ) {
                            map.setMaxZoomPreference(MAX_CAMERA_ZOOM.toDouble())
                            map.setMaxPitchPreference(MAX_CAMERA_TILT.toDouble())
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(53.0685, -4.0760))
                                .zoom(11.4)
                                .build()
                            status = "Imported Eryri package loaded"
                        }
                    }
                }
            },
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth(0.68f)
                .padding(start = 12.dp, top = 10.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("Eryri offline topo", style = MaterialTheme.typography.titleSmall)
                Text(status, style = MaterialTheme.typography.bodySmall)
                Text(
                    "z15 vectors • z16 relief • OS 10 m contours",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    "OSM / Protomaps • Mapterhorn • Ordnance Survey",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        currentMap?.let { map ->
            val tiltSelected = controlMode == Pass3CameraControl.TILT
            MapSpikeVerticalCameraControls(
                tiltSelected = tiltSelected,
                value = if (tiltSelected) liveTilt else liveZoom,
                valueRange = if (tiltSelected) 0f..MAX_CAMERA_TILT else MIN_CAMERA_ZOOM..MAX_CAMERA_ZOOM,
                onValueChange = { value -> applyPass3CameraValue(map, controlMode, value) },
                onTiltSelected = { controlMode = Pass3CameraControl.TILT },
                onZoomSelected = { controlMode = Pass3CameraControl.ZOOM },
                mapBackdropIsDark = false,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    // Run #111 physical feedback: raise the complete control cluster by about 2 cm.
                    .padding(end = 10.dp, bottom = 180.dp),
            )
        }
    }
}

private fun applyPass3CameraValue(
    map: MapLibreMap,
    mode: Pass3CameraControl,
    value: Float,
) {
    val builder = CameraPosition.Builder(map.cameraPosition)
    when (mode) {
        Pass3CameraControl.TILT -> builder.tilt(value.toDouble())
        Pass3CameraControl.ZOOM -> builder.zoom(value.toDouble())
    }
    map.cameraPosition = builder.build()
}

private enum class Pass3CameraControl {
    TILT,
    ZOOM,
}

private fun initialisePass3OfflineMapLibre(context: Context) {
    MapLibre.getInstance(context.applicationContext)
    MapLibre.setConnected(false)
}

private fun pass3OfflineEryriStyle(packages: InstalledOfflineMapPackage): String {
    val basemapPath = pass3JsonEscape(packages.basemap.absolutePath)
    val terrainPath = pass3JsonEscape(packages.terrain.absolutePath)
    val contourPath = pass3JsonEscape(packages.contours.absolutePath)
    val glyphRoot = pass3JsonEscape(packages.glyphDirectory.absolutePath)

    return """
        {
          "version": 8,
          "name": "TrailCharter Eryri offline topo pass 3",
          "center": [-4.0760, 53.0685],
          "zoom": 11.4,
          "glyphs": "file://$glyphRoot/{fontstack}/{range}.pbf",
          "sources": {
            "basemap": {
              "type": "vector",
              "url": "pmtiles://file://$basemapPath",
              "maxzoom": 15,
              "attribution": "© OpenStreetMap contributors / Protomaps"
            },
            "terrain": {
              "type": "raster-dem",
              "url": "pmtiles://file://$terrainPath",
              "tileSize": 512,
              "encoding": "terrarium",
              "maxzoom": 16,
              "attribution": "© Mapterhorn data sources"
            },
            "contours": {
              "type": "vector",
              "url": "pmtiles://file://$contourPath",
              "maxzoom": 14,
              "attribution": "Contains OS data © Crown copyright and database right 2026"
            }
          },
          "layers": [
            {
              "id": "background",
              "type": "background",
              "paint": { "background-color": "#EFEAE0" }
            },
            {
              "id": "natural",
              "type": "fill",
              "source": "basemap",
              "source-layer": "natural",
              "paint": {
                "fill-color": [
                  "match", ["get", "natural"],
                  "wood", "#C7D4BC",
                  "scrub", "#D3D8BD",
                  "wetland", "#D8E0D1",
                  "bare_rock", "#D1CCC2",
                  "sand", "#E6D8B4",
                  "#D8DFC9"
                ],
                "fill-opacity": 0.78
              }
            },
            {
              "id": "landuse",
              "type": "fill",
              "source": "basemap",
              "source-layer": "landuse",
              "paint": {
                "fill-color": [
                  "match", ["get", "kind"],
                  ["forest", "wood"], "#CAD7BF",
                  ["grass", "meadow", "park", "recreation_ground"], "#DDE2C8",
                  ["farmland", "farmyard"], "#E4DFC4",
                  ["residential"], "#E6E1D8",
                  "#E7E2D7"
                ],
                "fill-opacity": 0.62
              }
            },
            {
              "id": "hillshade",
              "type": "hillshade",
              "source": "terrain",
              "paint": {
                "hillshade-exaggeration": 0.48,
                "hillshade-shadow-color": "#5E574B",
                "hillshade-highlight-color": "#FFFDF5",
                "hillshade-accent-color": "#776F60"
              }
            },
            {
              "id": "contours-standard",
              "type": "line",
              "source": "contours",
              "source-layer": "contour_line",
              "minzoom": 12,
              "filter": ["!=", ["%", ["get", "property_value"], 50], 0],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#806F5C",
                "line-width": ["interpolate", ["linear"], ["zoom"], 12, 0.45, 15, 0.7, 17, 0.9],
                "line-opacity": ["interpolate", ["linear"], ["zoom"], 12, 0.25, 14, 0.38, 17, 0.46]
              }
            },
            {
              "id": "contours-index",
              "type": "line",
              "source": "contours",
              "source-layer": "contour_line",
              "minzoom": 11,
              "filter": ["==", ["%", ["get", "property_value"], 50], 0],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#6E5C49",
                "line-width": ["interpolate", ["linear"], ["zoom"], 11, 0.65, 14, 1.0, 17, 1.25],
                "line-opacity": 0.62
              }
            },
            {
              "id": "contour-labels",
              "type": "symbol",
              "source": "contours",
              "source-layer": "contour_line",
              "minzoom": 13,
              "filter": ["==", ["%", ["get", "property_value"], 50], 0],
              "layout": {
                "symbol-placement": "line",
                "symbol-spacing": 320,
                "text-field": ["concat", ["to-string", ["get", "property_value"]], " m"],
                "text-font": ["TrailCharterSans"],
                "text-size": 10.5,
                "text-max-angle": 25,
                "text-padding": 12,
                "text-allow-overlap": false
              },
              "paint": {
                "text-color": "#5F4F3F",
                "text-halo-color": "#EFEAE0",
                "text-halo-width": 1.25,
                "text-halo-blur": 0.25
              }
            },
            {
              "id": "water",
              "type": "fill",
              "source": "basemap",
              "source-layer": "water",
              "filter": ["==", ["geometry-type"], "Polygon"],
              "paint": { "fill-color": "#B8D5DC", "fill-opacity": 0.92 }
            },
            {
              "id": "water-rivers-canals",
              "type": "line",
              "source": "basemap",
              "source-layer": "water",
              "minzoom": 10,
              "filter": [
                "all",
                ["==", ["geometry-type"], "LineString"],
                ["match", ["get", "kind_detail"], ["river", "canal"], true, false]
              ],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#9BC7D2",
                "line-width": ["interpolate", ["linear"], ["zoom"], 10, 0.8, 14, 2.2, 16, 3.0],
                "line-opacity": 0.95
              }
            },
            {
              "id": "water-streams-drains",
              "type": "line",
              "source": "basemap",
              "source-layer": "water",
              "minzoom": 12,
              "filter": [
                "all",
                ["==", ["geometry-type"], "LineString"],
                ["match", ["get", "kind_detail"], ["stream", "ditch", "drain"], true, false]
              ],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#A8CED6",
                "line-width": ["interpolate", ["linear"], ["zoom"], 12, 0.55, 14, 1.2, 16, 1.8],
                "line-opacity": 0.9
              }
            },
            {
              "id": "buildings",
              "type": "fill",
              "source": "basemap",
              "source-layer": "buildings",
              "minzoom": 12,
              "paint": { "fill-color": "#CEC7BD", "fill-opacity": 0.78 }
            },
            {
              "id": "major-roads",
              "type": "line",
              "source": "basemap",
              "source-layer": "roads",
              "filter": ["match", ["get", "kind"], ["highway", "major_road"], true, false],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#A99985",
                "line-width": ["interpolate", ["linear"], ["zoom"], 9, 1.1, 14, 3.0, 16, 4.4],
                "line-opacity": 0.96
              }
            },
            {
              "id": "minor-roads",
              "type": "line",
              "source": "basemap",
              "source-layer": "roads",
              "minzoom": 10,
              "filter": ["match", ["get", "kind"], ["minor_road"], true, false],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#B8AA98",
                "line-width": ["interpolate", ["linear"], ["zoom"], 10, 0.7, 14, 1.9, 16, 2.8],
                "line-opacity": 0.94
              }
            },
            {
              "id": "other-roads",
              "type": "line",
              "source": "basemap",
              "source-layer": "roads",
              "minzoom": 11,
              "filter": [
                "all",
                ["!", ["match", ["get", "kind"], ["highway", "major_road", "minor_road", "path", "rail", "aerialway", "ferry"], true, false]],
                ["!", ["match", ["get", "kind_detail"], ["track", "path", "cycleway", "bridleway", "steps", "sidewalk", "crossing"], true, false]]
              ],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#C1B5A5",
                "line-width": ["interpolate", ["linear"], ["zoom"], 11, 0.5, 14, 1.3, 16, 2.0],
                "line-opacity": 0.9
              }
            },
            {
              "id": "tracks",
              "type": "line",
              "source": "basemap",
              "source-layer": "roads",
              "minzoom": 11,
              "filter": ["==", ["get", "kind_detail"], "track"],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#8E765F",
                "line-width": ["interpolate", ["linear"], ["zoom"], 11, 0.7, 14, 1.6, 16, 2.2],
                "line-dasharray": [3, 1.6],
                "line-opacity": 0.95
              }
            },
            {
              "id": "paths",
              "type": "line",
              "source": "basemap",
              "source-layer": "roads",
              "minzoom": 11,
              "filter": ["match", ["get", "kind_detail"], ["path", "cycleway", "bridleway", "steps", "sidewalk", "crossing"], true, false],
              "layout": { "line-cap": "round", "line-join": "round" },
              "paint": {
                "line-color": "#7F5F49",
                "line-width": ["interpolate", ["linear"], ["zoom"], 11, 0.7, 14, 1.6, 16, 2.2],
                "line-dasharray": [2, 1.5],
                "line-opacity": 0.98
              }
            },
            {
              "id": "boundaries",
              "type": "line",
              "source": "basemap",
              "source-layer": "boundaries",
              "paint": {
                "line-color": "#8D877C",
                "line-width": 0.7,
                "line-opacity": 0.35,
                "line-dasharray": [3, 2]
              }
            }
          ]
        }
    """.trimIndent()
}

private fun pass3JsonEscape(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")