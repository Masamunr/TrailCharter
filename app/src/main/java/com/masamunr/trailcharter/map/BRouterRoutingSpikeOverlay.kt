package com.masamunr.trailcharter.map

import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import btools.router.OsmTrack
import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.routing.BRouterRoutingEngine
import com.masamunr.trailcharter.routing.InstalledOfflineRoutingPackage
import com.masamunr.trailcharter.routing.RoutePlanningMode
import com.masamunr.trailcharter.routing.RouteWaypoint
import com.masamunr.trailcharter.routing.RoutingRequest
import com.masamunr.trailcharter.routing.TravelMode
import com.masamunr.trailcharter.routing.importEryriRoutingPackage
import com.masamunr.trailcharter.routing.loadInstalledEryriRoutingPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import kotlin.math.roundToInt

private const val ROUTE_SOURCE_ID = "trailcharter-brouter-test-route"
private const val ROUTE_LAYER_ID = "trailcharter-brouter-test-route-line"
private const val DEFAULT_ROUTE_OPACITY = 0.55f
private const val MIN_ROUTE_OPACITY = 0.10f
private const val MAX_ROUTE_OPACITY = 1.00f

private data class RoutingBenchmark(
    val distanceMetres: Double,
    val ascentMetres: Double?,
    val descentMetres: Double?,
    val durationSeconds: Long?,
    val calculationMillis: Long,
    val heapDeltaBytes: Long,
)

/**
 * Deliberately small technical UI for the first BRouter physical proof.
 *
 * It does not attempt to be the eventual TrailCharter route-planning interface. The fixed three
 * Eryri waypoints let us compare snapping/route quality and runtime measurements before designing
 * magnetic-route interactions around an engine that has not yet earned selection.
 */
@Composable
internal fun BRouterRoutingSpikeOverlay(
    map: MapLibreMap,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val scope = rememberCoroutineScope()
    var routingPackage by remember(applicationContext) {
        mutableStateOf(loadInstalledEryriRoutingPackage(applicationContext))
    }
    var importing by remember { mutableStateOf(false) }
    var calculating by remember { mutableStateOf(false) }
    var routeOpacity by remember { mutableStateOf(DEFAULT_ROUTE_OPACITY) }
    var message by remember {
        mutableStateOf(
            if (routingPackage == null) {
                "Routing data not installed"
            } else {
                "BRouter ${OsmTrack.version ?: "1.7.10"} WALK package ready"
            },
        )
    }
    var benchmark by remember { mutableStateOf<RoutingBenchmark?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            importing = true
            message = "Importing routing package…"
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        importEryriRoutingPackage(applicationContext, uri)
                    }
                }.onSuccess { installed ->
                    routingPackage = installed
                    importing = false
                    message = "BRouter 1.7.10 WALK package ready"
                }.onFailure { error ->
                    importing = false
                    message = "Routing import failed: ${error.message.orEmpty()}"
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(0.70f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("BRouter WALK spike", style = MaterialTheme.typography.titleSmall)
            Text(message, style = MaterialTheme.typography.bodySmall)

            benchmark?.let { result ->
                val eta = result.durationSeconds?.let { seconds ->
                    val minutes = (seconds + 30L) / 60L
                    "$minutes min"
                } ?: "n/a"
                Text(
                    "%.2f km • +%.0f / -%.0f m • %s".format(
                        result.distanceMetres / 1000.0,
                        result.ascentMetres ?: 0.0,
                        result.descentMetres ?: 0.0,
                        eta,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    "${result.calculationMillis} ms • heap Δ ${formatBytes(result.heapDeltaBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Route opacity ${(routeOpacity * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                )
                Slider(
                    value = routeOpacity,
                    onValueChange = { opacity ->
                        routeOpacity = opacity
                        updateRouteOpacity(map, opacity)
                    },
                    valueRange = MIN_ROUTE_OPACITY..MAX_ROUTE_OPACITY,
                )
            }

            val installed = routingPackage
            if (installed == null) {
                Button(
                    enabled = !importing,
                    onClick = {
                        // Some Android document providers label ZIP files inconsistently and hide
                        // them when OpenDocument is restricted to ZIP-specific MIME types. Show
                        // all documents here and rely on TrailCharter's manifest/hash validation
                        // to reject anything that is not the expected routing package.
                        importLauncher.launch(arrayOf("*/*"))
                    },
                ) {
                    Text(if (importing) "Importing…" else "Choose routing package")
                }
            } else {
                Button(
                    enabled = !calculating,
                    onClick = {
                        calculating = true
                        benchmark = null
                        message = "Routing Pen-y-Pass → Pyg Track → Yr Wyddfa…"
                        scope.launch {
                            runCatching {
                                calculateBenchmark(installed)
                            }.onSuccess { (route, metrics) ->
                                renderRoute(map, route.geometry.points, routeOpacity)
                                benchmark = metrics
                                calculating = false
                                message = "Magnetic three-point WALK route calculated"
                            }.onFailure { error ->
                                calculating = false
                                message = "Routing failed: ${error.message.orEmpty()}"
                            }
                        }
                    },
                ) {
                    Text(if (calculating) "Calculating…" else "Run 3-point WALK test")
                }
            }
        }
    }
}

private suspend fun calculateBenchmark(
    routingPackage: InstalledOfflineRoutingPackage,
): Pair<com.masamunr.trailcharter.routing.RoutingResult, RoutingBenchmark> {
    val runtime = Runtime.getRuntime()
    val heapBefore = runtime.totalMemory() - runtime.freeMemory()
    val started = SystemClock.elapsedRealtime()
    val result = BRouterRoutingEngine(routingPackage).calculateRoute(
        RoutingRequest(
            waypoints = listOf(
                RouteWaypoint(GeoPoint(latitude = 53.0806, longitude = -4.0207), "Pen-y-Pass"),
                RouteWaypoint(GeoPoint(latitude = 53.0765, longitude = -4.0445), "Pyg Track via"),
                RouteWaypoint(GeoPoint(latitude = 53.0685, longitude = -4.0762), "Yr Wyddfa"),
            ),
            travelMode = TravelMode.WALK,
            planningMode = RoutePlanningMode.MAGNETIC,
        ),
    )
    val elapsed = SystemClock.elapsedRealtime() - started
    val heapAfter = runtime.totalMemory() - runtime.freeMemory()

    return result to RoutingBenchmark(
        distanceMetres = result.estimate.distanceMetres,
        ascentMetres = result.estimate.ascentMetres,
        descentMetres = result.estimate.descentMetres,
        durationSeconds = result.estimate.durationSeconds,
        calculationMillis = elapsed,
        heapDeltaBytes = heapAfter - heapBefore,
    )
}

private fun renderRoute(map: MapLibreMap, points: List<GeoPoint>, opacity: Float) {
    val style = map.style ?: error("Map style is not ready")
    val coordinates = points.joinToString(separator = ",") { point ->
        "[${point.longitude},${point.latitude}]"
    }
    val geoJson = """{"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[$coordinates]}}"""

    val existingSource = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
    if (existingSource != null) {
        existingSource.setGeoJson(geoJson)
    } else {
        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, geoJson))
    }

    val existingLayer = style.getLayer(ROUTE_LAYER_ID)
    if (existingLayer == null) {
        style.addLayer(
            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                lineColor("#D14A3A"),
                lineWidth(5.0f),
                lineOpacity(opacity),
            ),
        )
    } else {
        existingLayer.setProperties(lineOpacity(opacity))
    }
}

private fun updateRouteOpacity(map: MapLibreMap, opacity: Float) {
    map.style?.getLayer(ROUTE_LAYER_ID)?.setProperties(lineOpacity(opacity))
}

private fun formatBytes(bytes: Long): String {
    val sign = if (bytes < 0L) "-" else "+"
    val absolute = kotlin.math.abs(bytes.toDouble())
    return when {
        absolute >= 1024.0 * 1024.0 -> "$sign%.1f MiB".format(absolute / (1024.0 * 1024.0))
        absolute >= 1024.0 -> "$sign%.1f KiB".format(absolute / 1024.0)
        else -> "$sign${absolute.toLong()} B"
    }
}
