package com.masamunr.trailcharter.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRoutingSpikeContractsTest {
    @Test
    fun pathRenderingUsesTheCompleteProtomapsPathKind() {
        assertEquals(
            """["all",["==",["get","kind"],"path"],["==",["get","kind_detail"],"track"]]""",
            PASS3_TRACK_FILTER_JSON,
        )
        assertEquals(
            """["all",["==",["get","kind"],"path"],["!",["match",["get","kind_detail"],["track","pier"],true,false]]]""",
            PASS3_PATH_FILTER_JSON,
        )
        assertTrue(PASS3_NAMED_WALKING_PATH_FILTER_JSON.contains("[\"get\",\"kind\"],\"path\""))
        assertTrue(PASS3_NAMED_WALKING_PATH_FILTER_JSON.contains("[\"has\",\"name\"]"))
    }

    @Test
    fun bothPhysicalRoutingScenariosRemainExposed() {
        assertEquals(
            listOf("Run Yr Wyddfa WALK test", "Run Moel Siabod WALK test"),
            routingSpikeScenarios.map { it.buttonLabel },
        )
        assertEquals(3, routingSpikeScenarios[0].waypoints.size)
        assertEquals(2, routingSpikeScenarios[1].waypoints.size)
        assertTrue(routingSpikeScenarios[1].waypoints.first().name.orEmpty().contains("Plas y Brenin"))
        assertTrue(routingSpikeScenarios[1].waypoints.last().name.orEmpty().contains("Moel Siabod"))
    }
}
