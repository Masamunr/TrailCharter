package com.masamunr.trailcharter.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.routing.BRouterRoutingEngine
import com.masamunr.trailcharter.routing.RoutePlanningMode
import com.masamunr.trailcharter.routing.RouteWaypoint
import com.masamunr.trailcharter.routing.RoutingRequest
import com.masamunr.trailcharter.routing.TravelMode
import com.masamunr.trailcharter.routing.loadInstalledEryriRoutingPackage
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource

internal enum class StagePointSelectionMode {
    START,
    FINISH,
    WAYPOINT,
}

internal data class StageRoutePlanDraft(
    val start: GeoPoint? = null,
    val finish: GeoPoint? = null,
    val waypoints: List<GeoPoint> = emptyList(),
    val snapToNetwork: Boolean = true,
    val distanceMetres: Double? = null,
    val ascentMetres: Double? = null,
    val descentMetres: Double? = null,
    val durationSeconds: Long? = null,
)

internal const val STAGE_PLANNER_POINT_SOURCE_ID = "trailcharter-stage-plan-points"
internal const val STAGE_PLANNER_POINT_LAYER_ID = "trailcharter-stage-plan-points-layer"
internal const val STAGE_PLANNER_ROUTE_SOURCE_ID = "trailcharter-stage-plan-route"
internal const val STAGE_PLANNER_ROUTE_LAYER_ID = "trailcharter-stage-plan-route-layer"

@Composable
internal fun StageRoutePlanningSpikeOverlay(
    map: MapLibreMap,
    initialDraft: StageRoutePlanDraft,
    onDone: (StageRoutePlanDraft) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val routingPackage = remember(context) { loadInstalledEryriRoutingPackage(context) }
    val scope = rememberCoroutineScope()

    var draft by remember(initialDraft) { mutableStateOf(initialDraft) }
    var selectionMode by remember { mutableStateOf(StagePointSelectionMode.START) }
    var message by remember {
        mutableStateOf("Choose Start, Finish or Waypoint, then tap the map")
    }
    var calculating by remember { mutableStateOf(false) }

    DisposableEffect(map, selectionMode, draft.snapToNetwork) {
        val listener = MapLibreMap.OnMapClickListener { latLng ->
            val point = GeoPoint(latitude = latLng.latitude, longitude = latLng.longitude)
            draft = when (selectionMode) {
                StagePointSelectionMode.START -> draft.copy(
                    start = point,
                    distanceMetres = null,
                    ascentMetres = null,
                    descentMetres = null,
                    durationSeconds = null,
                )
                StagePointSelectionMode.FINISH -> draft.copy(
                    finish = point,
                    distanceMetres = null,
                    ascentMetres = null,
                    descentMetres = null,
                    durationSeconds = null,
                )
                StagePointSelectionMode.WAYPOINT -> draft.copy(
                    waypoints = draft.waypoints + point,
                    distanceMetres = null,
                    ascentMetres = null,
                    descentMetres = null,
                    durationSeconds = null,
                )
            }
            renderStagePlannerPoints(map, draft)
            clearStagePlannerRoute(map)
            message = when (selectionMode) {
                StagePointSelectionMode.START -> "Start set at exact tapped coordinate"
                StagePointSelectionMode.FINISH -> "Finish set at exact tapped coordinate"
                StagePointSelectionMode.WAYPOINT -> "Waypoint ${draft.waypoints.size} added at exact tapped coordinate"
            }
            true
        }
        map.addOnMapClickListener(listener)
        renderStagePlannerPoints(map, draft)
        onDispose { map.removeOnMapClickListener(listener) }
    }

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 116.dp)
            .fillMaxWidth(0.78f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Plan stage on map", style = MaterialTheme.typography.titleSmall)
            Text(message, style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StageSelectionButton("Start", selectionMode == StagePointSelectionMode.START) {
                    selectionMode = StagePointSelectionMode.START
                }
                StageSelectionButton("Finish", selectionMode == StagePointSelectionMode.FINISH) {
                    selectionMode = StagePointSelectionMode.FINISH
                }
                StageSelectionButton("Waypoint", selectionMode == StagePointSelectionMode.WAYPOINT) {
                    selectionMode = StagePointSelectionMode.WAYPOINT
                }
            }

            Text(
                "Start ${draft.start?.shortCoordinate() ?: "not set"} • Finish ${draft.finish?.shortCoordinate() ?: "not set"} • ${draft.waypoints.size} waypoint(s)",
                style = MaterialTheme.typography.labelSmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Snap to routable network", style = MaterialTheme.typography.labelMedium)
                    Text(
                        if (draft.snapToNetwork) {
                            "On: guided route may snap selected points"
                        } else {
                            "Off: exact taps are preserved; no silent snapping"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = draft.snapToNetwork,
                    onCheckedChange = { enabled ->
                        draft = draft.copy(
                            snapToNetwork = enabled,
                            distanceMetres = null,
                            ascentMetres = null,
                            descentMetres = null,
                            durationSeconds = null,
                        )
                        clearStagePlannerRoute(map)
                        message = if (enabled) {
                            "Snapping enabled for guided routing"
                        } else {
                            "Snapping disabled; exact selected coordinates retained"
                        }
                    },
                )
            }

            if (draft.waypoints.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        draft = draft.copy(
                            waypoints = draft.waypoints.dropLast(1),
                            distanceMetres = null,
                            ascentMetres = null,
                            descentMetres = null,
                            durationSeconds = null,
                        )
                        renderStagePlannerPoints(map, draft)
                        clearStagePlannerRoute(map)
                        message = "Last waypoint removed"
                    },
                ) {
                    Text("Remove last waypoint")
                }
            }

            val canCalculate = draft.start != null && draft.finish != null && draft.snapToNetwork && routingPackage != null
            Button(
                enabled = canCalculate && !calculating,
                onClick = {
                    val start = draft.start ?: return@Button
                    val finish = draft.finish ?: return@Button
                    val installed = routingPackage ?: return@Button
                    calculating = true
                    message = "Calculating guided WALK route…"
                    scope.launch {
                        runCatching {
                            val routeWaypoints = buildList {
                                add(RouteWaypoint(start, "Start"))
                                draft.waypoints.forEachIndexed { index, point ->
                                    add(RouteWaypoint(point, "Waypoint ${index + 1}"))
                                }
                                add(RouteWaypoint(finish, "Finish"))
                            }
                            BRouterRoutingEngine(installed).calculateRoute(
                                RoutingRequest(
                                    waypoints = routeWaypoints,
                                    travelMode = TravelMode.WALK,
                                    planningMode = RoutePlanningMode.MAGNETIC,
                                ),
                            )
                        }.onSuccess { result ->
                            renderStagePlannerRoute(map, result.geometry.points)
                            draft = draft.copy(
                                distanceMetres = result.estimate.distanceMetres,
                                ascentMetres = result.estimate.ascentMetres,
                                descentMetres = result.estimate.descentMetres,
                                durationSeconds = result.estimate.durationSeconds,
                            )
                            message = "Guided WALK route calculated"
                            calculating = false
                        }.onFailure { error ->
                            message = "No guided route available: ${error.message.orEmpty()}"
                            calculating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (calculating) "Calculating…" else "Calculate WALK route")
            }

            if (!draft.snapToNetwork && draft.start != null && draft.finish != null) {
                Text(
                    "Guided calculation is deliberately disabled while Snap is off. Manual/direct routing will handle exact off-network points in a later slice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (routingPackage == null) {
                Text(
                    "BRouter routing data is not installed, so point selection can be tested but guided calculation is unavailable.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            draft.distanceMetres?.let { distance ->
                Text(
                    "%.2f km • +%.0f / -%.0f m • %s".format(
                        distance / 1000.0,
                        draft.ascentMetres ?: 0.0,
                        draft.descentMetres ?: 0.0,
                        draft.durationSeconds?.let { "${(it + 30L) / 60L} min" } ?: "ETA n/a",
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    enabled = draft.start != null && draft.finish != null,
                    onClick = { onDone(draft) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Use stage route")
                }
            }
        }
    }
}

@Composable
private fun StageSelectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

private fun GeoPoint.shortCoordinate(): String = "%.5f, %.5f".format(latitude, longitude)

private fun renderStagePlannerPoints(map: MapLibreMap, draft: StageRoutePlanDraft) {
    val style = map.style ?: return
    val features = buildList {
        draft.start?.let { add(pointFeature(it, "start")) }
        draft.waypoints.forEach { add(pointFeature(it, "waypoint")) }
        draft.finish?.let { add(pointFeature(it, "finish")) }
    }
    val geoJson = """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""

    val source = style.getSourceAs<GeoJsonSource>(STAGE_PLANNER_POINT_SOURCE_ID)
    if (source != null) {
        source.setGeoJson(geoJson)
    } else {
        style.addSource(GeoJsonSource(STAGE_PLANNER_POINT_SOURCE_ID, geoJson))
        style.addLayer(
            CircleLayer(STAGE_PLANNER_POINT_LAYER_ID, STAGE_PLANNER_POINT_SOURCE_ID).withProperties(
                circleRadius(7.0f),
                circleColor("#E53935"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2.0f),
            ),
        )
    }
}

private fun pointFeature(point: GeoPoint, role: String): String =
    """{"type":"Feature","properties":{"role":"$role"},"geometry":{"type":"Point","coordinates":[${point.longitude},${point.latitude}]}}"""

private fun renderStagePlannerRoute(map: MapLibreMap, points: List<GeoPoint>) {
    val style = map.style ?: return
    val coordinates = points.joinToString(",") { "[${it.longitude},${it.latitude}]" }
    val geoJson = """{"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[$coordinates]}}"""
    val source = style.getSourceAs<GeoJsonSource>(STAGE_PLANNER_ROUTE_SOURCE_ID)
    if (source != null) {
        source.setGeoJson(geoJson)
    } else {
        style.addSource(GeoJsonSource(STAGE_PLANNER_ROUTE_SOURCE_ID, geoJson))
        style.addLayer(
            LineLayer(STAGE_PLANNER_ROUTE_LAYER_ID, STAGE_PLANNER_ROUTE_SOURCE_ID).withProperties(
                lineColor(PASS4_TRAILCHARTER_ROUTE_COLOR),
                lineWidth(5.0f),
                lineOpacity(0.78f),
            ),
        )
    }
}

private fun clearStagePlannerRoute(map: MapLibreMap) {
    map.style?.getSourceAs<GeoJsonSource>(STAGE_PLANNER_ROUTE_SOURCE_ID)?.setGeoJson(
        """{"type":"FeatureCollection","features":[]}""",
    )
}
