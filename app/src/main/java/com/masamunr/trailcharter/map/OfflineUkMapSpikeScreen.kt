package com.masamunr.trailcharter.map

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val BASEMAP_ASSET = "map_spike/eryri-basemap.pmtiles"
private const val TERRAIN_ASSET = "map_spike/eryri-terrain.pmtiles"
private const val BASEMAP_FILE = "eryri-basemap.pmtiles"
private const val TERRAIN_FILE = "eryri-terrain.pmtiles"

/**
 * Real-data physical renderer spike.
 *
 * Both map packages are embedded in the debug APK by CI and copied into app-managed storage on
 * first use. MapLibre is forced disconnected before MapView creation, so this screen performs no
 * map/style/tile networking and does not require INTERNET or ACCESS_NETWORK_STATE.
 */
@Composable
internal fun OfflineUkMapSpikeScreen() {
    val context = LocalContext.current
    var packages by remember { mutableStateOf<OfflineMapPackages?>(null) }
    var preparationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(context.applicationContext) {
        runCatching {
            withContext(Dispatchers.IO) {
                ensureOfflineMapPackages(context.applicationContext)
            }
        }.onSuccess {
            packages = it
        }.onFailure { error ->
            preparationError = "${error::class.java.simpleName}: ${error.message.orEmpty()}"
        }
    }

    when {
        preparationError != null -> MapStatusScreen(
            title = "Offline map package error",
            body = preparationError.orEmpty(),
        )

        packages == null -> MapStatusScreen(
            title = "Preparing offline Eryri map",
            body = "Copying the embedded vector and terrain packages into TrailCharter's private storage…",
        )

        else -> OfflineEryriMap(packages = packages!!)
    }
}

@Composable
private fun OfflineEryriMap(packages: OfflineMapPackages) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var currentMapView by remember { mutableStateOf<MapView?>(null) }
    var currentMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var status by remember { mutableStateOf("Starting offline renderer…") }

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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                initialiseOfflineMapLibre(viewContext)
                MapView(
                    viewContext,
                    MapLibreMapOptions.createFromAttributes(viewContext)
                        .foregroundLoadColor(Color.rgb(239, 235, 226))
                        .textureMode(false),
                ).also { mapView ->
                    currentMapView = mapView
                    status = "Loading embedded map + relief…"
                    mapView.getMapAsync { map ->
                        currentMap = map
                        status = "Renderer ready; loading local style…"
                        map.setStyle(
                            Style.Builder().fromJson(offlineEryriStyle(packages)),
                        ) {
                            map.setMaxZoomPreference(17.0)
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(53.0685, -4.0760))
                                .zoom(11.4)
                                .build()
                            status = "Offline Eryri map loaded"
                        }
                    }
                }
            },
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(12.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text("TrailCharter offline UK map spike", style = MaterialTheme.typography.titleSmall)
                Text(status, style = MaterialTheme.typography.bodySmall)
                Text(
                    "Eryri • z15 paths/roads/terrain + local hillshade",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Map © OpenStreetMap contributors / Protomaps • relief © Mapterhorn data sources",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        currentMap?.let { map ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("View", style = MaterialTheme.typography.labelMedium)
                    FilledTonalButton(onClick = { applyViewPreset(map, tilt = 0.0) }) {
                        Text("Flat")
                    }
                    FilledTonalButton(onClick = { applyViewPreset(map, tilt = 30.0) }) {
                        Text("Low")
                    }
                    FilledTonalButton(onClick = { applyViewPreset(map, tilt = 50.0) }) {
                        Text("Terrain")
                    }
                    FilledTonalButton(onClick = { applyViewPreset(map, tilt = 0.0, resetNorth = true) }) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}

private fun applyViewPreset(map: MapLibreMap, tilt: Double, resetNorth: Boolean = false) {
    val current = map.cameraPosition
    map.cameraPosition = CameraPosition.Builder()
        .target(current.target)
        .zoom(current.zoom)
        .tilt(tilt)
        .bearing(if (resetNorth) 0.0 else current.bearing)
        .build()
}

@Composable
private fun MapStatusScreen(title: String, body: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private data class OfflineMapPackages(
    val basemap: File,
    val terrain: File,
)

private fun ensureOfflineMapPackages(context: Context): OfflineMapPackages {
    val packageDir = context.filesDir.resolve("map_spike").also { it.mkdirs() }
    return OfflineMapPackages(
        basemap = copyAssetIfNeeded(context, BASEMAP_ASSET, packageDir.resolve(BASEMAP_FILE)),
        terrain = copyAssetIfNeeded(context, TERRAIN_ASSET, packageDir.resolve(TERRAIN_FILE)),
    )
}

private fun copyAssetIfNeeded(context: Context, assetPath: String, destination: File): File {
    val assetLength = context.assets.openFd(assetPath).use { it.length }
    if (!destination.exists() || destination.length() != assetLength) {
        val temporary = File(destination.parentFile, "${destination.name}.tmp")
        context.assets.open(assetPath).use { input ->
            temporary.outputStream().use { output -> input.copyTo(output) }
        }
        check(temporary.length() == assetLength) {
            "Incomplete embedded map package copy: $assetPath"
        }
        if (destination.exists()) destination.delete()
        check(temporary.renameTo(destination)) {
            "Could not install embedded map package: $assetPath"
        }
    }
    return destination
}

private fun initialiseOfflineMapLibre(context: Context) {
    MapLibre.getInstance(context.applicationContext)
    MapLibre.setConnected(false)
}

private fun offlineEryriStyle(packages: OfflineMapPackages): String {
    val basemapPath = jsonEscape(packages.basemap.absolutePath)
    val terrainPath = jsonEscape(packages.terrain.absolutePath)

    return """
        {
          "version": 8,
          "name": "TrailCharter Eryri offline topo spike",
          "center": [-4.0760, 53.0685],
          "zoom": 11.4,
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
              "maxzoom": 15,
              "attribution": "© Mapterhorn data sources"
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

private fun jsonEscape(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
