package com.masamunr.trailcharter

import com.masamunr.trailcharter.data.adventure.AdventureEntity
import com.masamunr.trailcharter.data.adventure.StageEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanningSessionChangesTest {
    private val adventure = AdventureEntity(
        id = 1,
        title = "Cadair Idris",
        summary = "Day walk",
        startDateEpochDay = 100,
        endDateEpochDay = 101,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 1,
    )

    private val persistedStage = StageEntity(
        id = 10,
        adventureId = 1,
        title = "Summit",
        position = 0,
        isComplete = false,
        completedAtEpochMillis = null,
    )

    private fun draftStage(
        complete: Boolean = false,
        completedAtEpochMillis: Long? = null,
    ) = DraftStage(
        key = "stage-10",
        persistedId = 10,
        title = "Summit",
        isComplete = complete,
        completedAtEpochMillis = completedAtEpochMillis,
    )

    @Test
    fun unchangedPlanningSessionIsClean() {
        assertFalse(
            planningSessionHasChanges(
                adventure = adventure,
                persistedStages = listOf(persistedStage),
                title = adventure.title,
                summary = adventure.summary,
                startDateEpochDay = adventure.startDateEpochDay,
                endDateEpochDay = adventure.endDateEpochDay,
                draftStages = listOf(draftStage()),
            ),
        )
    }

    @Test
    fun stageCompletionMakesPlanningSessionDirty() {
        assertTrue(
            planningSessionHasChanges(
                adventure = adventure,
                persistedStages = listOf(persistedStage),
                title = adventure.title,
                summary = adventure.summary,
                startDateEpochDay = adventure.startDateEpochDay,
                endDateEpochDay = adventure.endDateEpochDay,
                draftStages = listOf(draftStage(complete = true, completedAtEpochMillis = 1234)),
            ),
        )
    }

    @Test
    fun addedStageMakesPlanningSessionDirty() {
        assertTrue(
            planningSessionHasChanges(
                adventure = adventure,
                persistedStages = listOf(persistedStage),
                title = adventure.title,
                summary = adventure.summary,
                startDateEpochDay = adventure.startDateEpochDay,
                endDateEpochDay = adventure.endDateEpochDay,
                draftStages = listOf(
                    draftStage(),
                    DraftStage(
                        key = "draft-1",
                        persistedId = null,
                        title = "Return to car",
                        isComplete = false,
                        completedAtEpochMillis = null,
                    ),
                ),
            ),
        )
    }
}
