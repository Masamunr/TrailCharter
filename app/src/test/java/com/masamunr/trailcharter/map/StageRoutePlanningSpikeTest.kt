package com.masamunr.trailcharter.map

import com.masamunr.trailcharter.geo.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class StageRoutePlanningSpikeTest {
    @Test
    fun snapOffPreservesTheExactSelectedCoordinate() {
        val exactTap = GeoPoint(53.07123456789, -4.05123456789)
        val original = StageRoutePlanDraft(
            snapToNetwork = false,
            distanceMetres = 1_000.0,
            durationSeconds = 900,
            routeGeometry = listOf(GeoPoint(53.0, -4.0), GeoPoint(53.1, -4.1)),
        )

        val updated = stageDraftAfterMapTap(original, StagePointSelectionMode.START, exactTap)

        assertEquals(exactTap, updated.start)
        assertFalse(updated.snapToNetwork)
        assertNull(updated.distanceMetres)
        assertNull(updated.durationSeconds)
        assertEquals(emptyList<GeoPoint>(), updated.routeGeometry)
    }

    @Test
    fun mapTapsKeepWaypointInsertionOrder() {
        val first = GeoPoint(53.08, -4.08)
        val second = GeoPoint(53.07, -4.07)
        val third = GeoPoint(53.06, -4.06)

        val draft = listOf(first, second, third).fold(StageRoutePlanDraft()) { current, point ->
            stageDraftAfterMapTap(current, StagePointSelectionMode.WAYPOINT, point)
        }

        assertEquals(listOf(first, second, third), draft.waypoints)
    }

    @Test
    fun durationFormattingUsesHumanReadableHoursAndMinutes() {
        assertEquals("42 min", formatRouteDuration(42 * 60L))
        assertEquals("1 hr", formatRouteDuration(60 * 60L))
        assertEquals("1 hr 46 min", formatRouteDuration(106 * 60L))
        assertEquals("5 hr 12 min", formatRouteDuration(312 * 60L))
    }
}
