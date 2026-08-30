package com.masamunr.trailcharter.data.adventure

import com.masamunr.trailcharter.geo.GeoPoint
import com.masamunr.trailcharter.geo.RouteGeometry
import com.masamunr.trailcharter.routing.RoutePlanningMode
import com.masamunr.trailcharter.routing.TravelMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StageRoutePersistenceTest {
    private val initialRoute = PersistedStageRoute(
        stageId = 42,
        start = GeoPoint(53.080001234, -4.070001234),
        finish = GeoPoint(53.060009876, -4.040009876),
        waypoints = listOf(
            GeoPoint(53.075, -4.065),
            GeoPoint(53.070, -4.055),
        ),
        snapToNetwork = false,
        planningMode = RoutePlanningMode.MAGNETIC,
        travelMode = TravelMode.WALK,
        geometry = RouteGeometry(
            listOf(
                GeoPoint(53.080001234, -4.070001234),
                GeoPoint(53.070, -4.055),
                GeoPoint(53.060009876, -4.040009876),
            ),
        ),
        distanceMetres = 5_430.5,
        ascentMetres = 730.0,
        descentMetres = 82.0,
        durationSeconds = 6_360,
    )

    @Test
    fun waypointOrderingAndRoundTripAreLossless() = runBlocking {
        val dao = FakeStageRouteDao()
        val repository = StageRouteRepository(dao) { 1_000L }

        repository.saveStageRoute(initialRoute)

        assertEquals(initialRoute, repository.getStageRoute(initialRoute.stageId))
        assertEquals(
            listOf("START", "WAYPOINT", "WAYPOINT", "FINISH"),
            dao.controlPoints.sortedBy { it.position }.map { it.role },
        )
        assertEquals(listOf(0, 1, 2, 3), dao.controlPoints.sortedBy { it.position }.map { it.position })
    }

    @Test
    fun replacementUpdatesOneStageRouteAndRemovesOldPoints() = runBlocking {
        var now = 1_000L
        val dao = FakeStageRouteDao()
        val repository = StageRouteRepository(dao) { now }
        repository.saveStageRoute(initialRoute)
        val originalCreatedAt = requireNotNull(dao.route).createdAtEpochMillis

        now = 2_000L
        val replacement = initialRoute.copy(
            waypoints = listOf(GeoPoint(53.071, -4.051)),
            geometry = RouteGeometry(
                listOf(
                    initialRoute.start,
                    GeoPoint(53.066, -4.047),
                    initialRoute.finish,
                ),
            ),
            distanceMetres = 4_900.0,
            durationSeconds = 5_400,
        )
        repository.saveStageRoute(replacement)

        assertEquals(replacement, repository.getStageRoute(initialRoute.stageId))
        assertEquals(3, dao.controlPoints.size)
        assertEquals(3, dao.geometryPoints.size)
        assertEquals(originalCreatedAt, dao.route?.createdAtEpochMillis)
        assertEquals(2_000L, dao.route?.updatedAtEpochMillis)
    }

    @Test
    fun removingRouteRemovesThePersistedAggregate() = runBlocking {
        val dao = FakeStageRouteDao()
        val repository = StageRouteRepository(dao) { 1_000L }
        repository.saveStageRoute(initialRoute)

        repository.removeStageRoute(initialRoute.stageId)

        assertNull(repository.getStageRoute(initialRoute.stageId))
        assertTrue(dao.controlPoints.isEmpty())
        assertTrue(dao.geometryPoints.isEmpty())
    }

    @Test
    fun migrationAddsOnlyRouteTablesAndNeverRewritesExistingAdventureTables() {
        val statements = TrailCharterDatabase.MIGRATION_2_3_STATEMENTS

        assertEquals(3, statements.size)
        assertTrue(statements.any { "`stage_routes`" in it })
        assertTrue(statements.any { "`stage_route_control_points`" in it })
        assertTrue(statements.any { "`stage_route_geometry_points`" in it })
        assertFalse(statements.any { "ALTER TABLE" in it.uppercase() || "DROP TABLE" in it.uppercase() })
        assertFalse(statements.any { "CREATE TABLE IF NOT EXISTS `adventures`" in it })
        assertFalse(statements.any { "CREATE TABLE IF NOT EXISTS `stages`" in it })
        assertFalse(statements.any { "CREATE TABLE IF NOT EXISTS `itinerary_items`" in it })
    }

    private class FakeStageRouteDao : StageRouteDao {
        var route: StageRouteEntity? = null
        val controlPoints = mutableListOf<StageRouteControlPointEntity>()
        val geometryPoints = mutableListOf<StageRouteGeometryPointEntity>()
        private val observed = MutableStateFlow<StageRouteAggregate?>(null)

        override fun observeStageRoute(stageId: Long): Flow<StageRouteAggregate?> = observed

        override suspend fun getStageRoute(stageId: Long): StageRouteAggregate? =
            aggregate()?.takeIf { it.route.stageId == stageId }

        override suspend fun getStageRouteEntity(stageId: Long): StageRouteEntity? =
            route?.takeIf { it.stageId == stageId }

        override suspend fun upsertStageRoute(route: StageRouteEntity) {
            this.route = route
            publish()
        }

        override suspend fun insertControlPoints(points: List<StageRouteControlPointEntity>) {
            controlPoints += points
            publish()
        }

        override suspend fun insertGeometryPoints(points: List<StageRouteGeometryPointEntity>) {
            geometryPoints += points
            publish()
        }

        override suspend fun deleteControlPoints(stageId: Long) {
            controlPoints.removeAll { it.stageId == stageId }
            publish()
        }

        override suspend fun deleteGeometryPoints(stageId: Long) {
            geometryPoints.removeAll { it.stageId == stageId }
            publish()
        }

        override suspend fun deleteStageRoute(stageId: Long) {
            route = route?.takeUnless { it.stageId == stageId }
            controlPoints.removeAll { it.stageId == stageId }
            geometryPoints.removeAll { it.stageId == stageId }
            publish()
        }

        private fun aggregate(): StageRouteAggregate? = route?.let {
            StageRouteAggregate(it, controlPoints.toList(), geometryPoints.toList())
        }

        private fun publish() {
            observed.value = aggregate()
        }
    }
}
