package com.masamunr.trailcharter.routing

import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.geo.RouteGeometry

enum class TravelMode {
    WALK,
    BICYCLE,
    DRIVE,
}

enum class RoutePlanningMode {
    MAGNETIC,
    DIRECT,
}

data class RouteWaypoint(
    val point: GeoPoint,
    val name: String? = null,
)

data class RoutingRequest(
    val waypoints: List<RouteWaypoint>,
    val travelMode: TravelMode,
    val planningMode: RoutePlanningMode = RoutePlanningMode.MAGNETIC,
) {
    init {
        require(waypoints.size >= 2) { "Routing requires at least a start and end point" }
    }
}

data class RouteEstimate(
    val distanceMetres: Double,
    val durationSeconds: Long?,
    val ascentMetres: Double? = null,
    val descentMetres: Double? = null,
)

data class RoutingResult(
    val geometry: RouteGeometry,
    val estimate: RouteEstimate,
    val snappedWaypoints: List<RouteWaypoint>,
)

data class RoutingEngineCapabilities(
    val travelModes: Set<TravelMode>,
    val supportsOfflineRouting: Boolean,
    val supportsWaypointSnapping: Boolean,
    val supportsMapMatching: Boolean,
    val supportsElevationAwareRouting: Boolean,
    val canConsumeLiveTraffic: Boolean,
)

interface RoutingEngineBoundary {
    val capabilities: RoutingEngineCapabilities

    suspend fun calculateRoute(request: RoutingRequest): RoutingResult
}
