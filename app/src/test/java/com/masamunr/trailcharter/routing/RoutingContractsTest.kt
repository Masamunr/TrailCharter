package com.masamunr.trailcharter.routing

import com.masamunr.trailcharter.geo.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RoutingContractsTest {
    @Test
    fun magneticPlanningIsTheDefaultRoutePlanningMode() {
        val request = RoutingRequest(
            waypoints = listOf(
                RouteWaypoint(GeoPoint(52.707, -2.754)),
                RouteWaypoint(GeoPoint(52.711, -2.743)),
            ),
            travelMode = TravelMode.WALK,
        )

        assertEquals(RoutePlanningMode.MAGNETIC, request.planningMode)
    }

    @Test
    fun routingRequiresStartAndEndPoints() {
        assertThrows(IllegalArgumentException::class.java) {
            RoutingRequest(
                waypoints = listOf(RouteWaypoint(GeoPoint(52.707, -2.754))),
                travelMode = TravelMode.WALK,
            )
        }
    }

    @Test
    fun geographicCoordinatesAreValidated() {
        assertThrows(IllegalArgumentException::class.java) {
            GeoPoint(91.0, 0.0)
        }
    }
}
