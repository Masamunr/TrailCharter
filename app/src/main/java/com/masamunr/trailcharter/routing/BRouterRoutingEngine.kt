package com.masamunr.trailcharter.routing

import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.geo.RouteGeometry
import btools.router.OsmNodeNamed
import btools.router.RoutingContext
import btools.router.RoutingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

internal class BRouterRoutingEngine(
    private val packageFiles: InstalledOfflineRoutingPackage,
) : RoutingEngineBoundary {

    override val capabilities = RoutingEngineCapabilities(
        travelModes = setOf(TravelMode.WALK),
        supportsOfflineRouting = true,
        supportsWaypointSnapping = true,
        supportsMapMatching = false,
        supportsElevationAwareRouting = true,
        canConsumeLiveTraffic = false,
    )

    override suspend fun calculateRoute(request: RoutingRequest): RoutingResult = withContext(Dispatchers.Default) {
        require(request.travelMode == TravelMode.WALK) {
            "BRouter spike currently supports WALK only"
        }
        require(request.planningMode == RoutePlanningMode.MAGNETIC) {
            "BRouter spike currently supports magnetic routing only"
        }
        check(packageFiles.segmentDirectory.isDirectory) { "BRouter segment directory is missing" }
        check(packageFiles.profile.isFile) { "BRouter walking profile is missing" }
        check(packageFiles.lookups.isFile) { "BRouter lookups.dat is missing" }

        val brouterWaypoints = request.waypoints.mapIndexed { index, waypoint ->
            OsmNodeNamed().apply {
                ilon = toBRouterLongitude(waypoint.point.longitude)
                ilat = toBRouterLatitude(waypoint.point.latitude)
                name = when (index) {
                    0 -> "from"
                    request.waypoints.lastIndex -> "to"
                    else -> "via$index"
                }
            }
        }

        val routingContext = RoutingContext().apply {
            // When profileBaseDir is unset BRouter intentionally resolves lookups.dat beside the
            // absolute localFunction file, which matches the imported package layout.
            localFunction = packageFiles.profile.absolutePath
        }
        val engine = RoutingEngine(
            null,
            null,
            packageFiles.segmentDirectory,
            brouterWaypoints,
            routingContext,
        ).apply {
            quite = true
        }

        engine.doRun(MAX_ROUTING_TIME_MILLIS)
        engine.errorMessage?.let { error("BRouter route failed: $it") }
        val track = requireNotNull(engine.foundTrack) { "BRouter returned no route" }

        val points = track.nodes.map { node ->
            GeoPoint(
                latitude = fromBRouterLatitude(node.iLat),
                longitude = fromBRouterLongitude(node.iLon),
            )
        }
        check(points.size >= 2) { "BRouter route contained fewer than two geometry points" }

        var descent = 0.0
        var previousElevation: Double? = null
        track.nodes.forEach { node ->
            if (node.sElev != Short.MIN_VALUE) {
                val elevation = node.elev
                previousElevation?.let { previous ->
                    if (elevation < previous) descent += previous - elevation
                }
                previousElevation = elevation
            }
        }

        val geometry = RouteGeometry(points)
        RoutingResult(
            geometry = geometry,
            estimate = RouteEstimate(
                distanceMetres = track.distance.toDouble(),
                durationSeconds = track.totalSeconds.toLong().takeIf { it > 0L },
                ascentMetres = track.ascend.toDouble(),
                descentMetres = descent,
            ),
            // The generic TrailCharter contract needs observable snapped points. BRouter exposes
            // its calculated track cleanly but not a simple ordered corrected-waypoint list here,
            // so the spike reports the nearest routed geometry point for each requested waypoint.
            snappedWaypoints = request.waypoints.map { requested ->
                RouteWaypoint(
                    point = nearestGeometryPoint(requested.point, points),
                    name = requested.name,
                )
            },
        )
    }

    private fun nearestGeometryPoint(target: GeoPoint, points: List<GeoPoint>): GeoPoint =
        points.minBy { point ->
            val latDelta = point.latitude - target.latitude
            val lonDelta = point.longitude - target.longitude
            latDelta * latDelta + lonDelta * lonDelta
        }

    private fun toBRouterLongitude(longitude: Double): Int =
        (longitude * 1_000_000.0 + 180_000_000.0).roundToInt()

    private fun toBRouterLatitude(latitude: Double): Int =
        (latitude * 1_000_000.0 + 90_000_000.0).roundToInt()

    private fun fromBRouterLongitude(ilon: Int): Double =
        (ilon - 180_000_000) / 1_000_000.0

    private fun fromBRouterLatitude(ilat: Int): Double =
        (ilat - 90_000_000) / 1_000_000.0

    private companion object {
        const val MAX_ROUTING_TIME_MILLIS = 60_000L
    }
}
