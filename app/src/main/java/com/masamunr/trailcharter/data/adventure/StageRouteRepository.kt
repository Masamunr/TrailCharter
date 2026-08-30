package com.masamunr.trailcharter.data.adventure

import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.geo.RouteGeometry
import com.masamunr.trailcharter.routing.RoutePlanningMode
import com.masamunr.trailcharter.routing.TravelMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class StageRoutePointRole {
    START,
    WAYPOINT,
    FINISH,
}

data class PersistedStageRoute(
    val stageId: Long,
    val start: GeoPoint,
    val finish: GeoPoint,
    val waypoints: List<GeoPoint>,
    val snapToNetwork: Boolean,
    val planningMode: RoutePlanningMode,
    val travelMode: TravelMode,
    val geometry: RouteGeometry?,
    val distanceMetres: Double?,
    val ascentMetres: Double?,
    val descentMetres: Double?,
    val durationSeconds: Long?,
)

class StageRouteRepository(
    private val dao: StageRouteDao,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) {
    fun observeStageRoute(stageId: Long): Flow<PersistedStageRoute?> =
        dao.observeStageRoute(stageId).map { aggregate -> aggregate?.toPersistedStageRoute() }

    suspend fun getStageRoute(stageId: Long): PersistedStageRoute? =
        dao.getStageRoute(stageId)?.toPersistedStageRoute()

    suspend fun saveStageRoute(route: PersistedStageRoute) {
        require(route.stageId > 0) { "A persisted Stage is required" }
        require(route.distanceMetres == null || route.distanceMetres >= 0.0) { "Distance cannot be negative" }
        require(route.durationSeconds == null || route.durationSeconds >= 0L) { "Duration cannot be negative" }
        val now = nowEpochMillis()
        dao.replaceStageRoute(
            route = StageRouteEntity(
                stageId = route.stageId,
                planningMode = route.planningMode.name,
                travelMode = route.travelMode.name,
                snapToNetwork = route.snapToNetwork,
                distanceMetres = route.distanceMetres,
                ascentMetres = route.ascentMetres,
                descentMetres = route.descentMetres,
                durationSeconds = route.durationSeconds,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
            controlPoints = route.toControlPointEntities(),
            geometryPoints = route.toGeometryPointEntities(),
        )
    }

    suspend fun removeStageRoute(stageId: Long) {
        dao.deleteStageRoute(stageId)
    }
}

internal fun PersistedStageRoute.toControlPointEntities(): List<StageRouteControlPointEntity> {
    val selectedPoints = buildList {
        add(StageRoutePointRole.START to start)
        waypoints.forEach { add(StageRoutePointRole.WAYPOINT to it) }
        add(StageRoutePointRole.FINISH to finish)
    }
    return selectedPoints.mapIndexed { position, (role, point) ->
        StageRouteControlPointEntity(
            stageId = stageId,
            position = position,
            role = role.name,
            latitude = point.latitude,
            longitude = point.longitude,
        )
    }
}

internal fun PersistedStageRoute.toGeometryPointEntities(): List<StageRouteGeometryPointEntity> =
    geometry?.points.orEmpty().mapIndexed { position, point ->
        StageRouteGeometryPointEntity(
            stageId = stageId,
            position = position,
            latitude = point.latitude,
            longitude = point.longitude,
        )
    }

internal fun StageRouteAggregate.toPersistedStageRoute(): PersistedStageRoute {
    val orderedControls = controlPoints.sortedBy { it.position }
    require(orderedControls.size >= 2) { "A Stage route requires Start and Finish" }
    require(orderedControls.first().role == StageRoutePointRole.START.name) { "Stage route must start with Start" }
    require(orderedControls.last().role == StageRoutePointRole.FINISH.name) { "Stage route must end with Finish" }
    require(orderedControls.drop(1).dropLast(1).all { it.role == StageRoutePointRole.WAYPOINT.name }) {
        "Intermediate Stage route points must be waypoints"
    }
    require(orderedControls.map { it.position } == orderedControls.indices.toList()) {
        "Stage route control-point positions must be contiguous"
    }

    val orderedGeometry = geometryPoints.sortedBy { it.position }
    require(orderedGeometry.isEmpty() || orderedGeometry.size >= 2) {
        "Stored route geometry must be empty or contain at least two points"
    }
    require(orderedGeometry.map { it.position } == orderedGeometry.indices.toList()) {
        "Stage route geometry positions must be contiguous"
    }

    fun StageRouteControlPointEntity.point() = GeoPoint(latitude, longitude)
    val geometry = orderedGeometry.takeIf { it.isNotEmpty() }?.let { points ->
        RouteGeometry(points.map { GeoPoint(it.latitude, it.longitude) })
    }

    return PersistedStageRoute(
        stageId = route.stageId,
        start = orderedControls.first().point(),
        finish = orderedControls.last().point(),
        waypoints = orderedControls.drop(1).dropLast(1).map { it.point() },
        snapToNetwork = route.snapToNetwork,
        planningMode = RoutePlanningMode.valueOf(route.planningMode),
        travelMode = TravelMode.valueOf(route.travelMode),
        geometry = geometry,
        distanceMetres = route.distanceMetres,
        ascentMetres = route.ascentMetres,
        descentMetres = route.descentMetres,
        durationSeconds = route.durationSeconds,
    )
}
