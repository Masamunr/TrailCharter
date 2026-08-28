package com.masamunr.trailcharter.data.adventure

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AdventureRepository(
    private val dao: AdventureDao,
) {
    fun observeAdventureSummaries(): Flow<List<AdventureSummaryRow>> = dao.observeAdventureSummaries()

    fun observeAdventure(adventureId: Long): Flow<AdventureEntity?> = dao.observeAdventure(adventureId)

    fun observeStages(adventureId: Long): Flow<List<StageEntity>> = dao.observeStages(adventureId)

    fun observeItineraryItems(adventureId: Long): Flow<List<ItineraryItemEntity>> =
        dao.observeItineraryItems(adventureId)

    fun observePlanningSession(adventureId: Long): Flow<AdventurePlanningSnapshot?> =
        combine(
            dao.observeAdventure(adventureId),
            dao.observeStages(adventureId),
        ) { adventure, stages ->
            adventure?.let { AdventurePlanningSnapshot(it, stages) }
        }

    suspend fun createAdventure(
        title: String,
        summary: String,
        startDateEpochDay: Long?,
        endDateEpochDay: Long?,
    ): Long {
        require(title.isNotBlank()) { "Adventure title is required" }
        val now = System.currentTimeMillis()
        return dao.insertAdventure(
            AdventureEntity(
                title = title.trim(),
                summary = summary.trim(),
                startDateEpochDay = startDateEpochDay,
                endDateEpochDay = endDateEpochDay,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun savePlanningSession(
        snapshot: AdventurePlanningSnapshot,
        title: String,
        summary: String,
        startDateEpochDay: Long?,
        endDateEpochDay: Long?,
        stages: List<PlanningStageDraft>,
    ) {
        require(title.isNotBlank()) { "Adventure title is required" }

        val persistedById = snapshot.stages.associateBy { it.id }
        val retainedIds = stages.mapNotNull { it.persistedId }.toSet()
        val stageIdsToDelete = snapshot.stages
            .map { it.id }
            .filterNot(retainedIds::contains)

        val stagesToUpdate = stages.mapIndexedNotNull { position, draft ->
            val persistedId = draft.persistedId ?: return@mapIndexedNotNull null
            val persisted = persistedById[persistedId] ?: return@mapIndexedNotNull null
            persisted.copy(
                title = draft.title.trim(),
                position = position,
                isComplete = draft.isComplete,
                completedAtEpochMillis = draft.completedAtEpochMillis,
            )
        }

        val stagesToInsert = stages.mapIndexedNotNull { position, draft ->
            if (draft.persistedId != null) return@mapIndexedNotNull null
            StageEntity(
                adventureId = snapshot.adventure.id,
                title = draft.title.trim(),
                position = position,
                isComplete = draft.isComplete,
                completedAtEpochMillis = draft.completedAtEpochMillis,
            )
        }

        dao.savePlanningSession(
            adventure = snapshot.adventure.copy(
                title = title.trim(),
                summary = summary.trim(),
                startDateEpochDay = startDateEpochDay,
                endDateEpochDay = endDateEpochDay,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
            stagesToUpdate = stagesToUpdate,
            stageIdsToDelete = stageIdsToDelete,
            stagesToInsert = stagesToInsert,
        )
    }

    suspend fun deleteAdventure(adventureId: Long) {
        dao.deleteAdventure(adventureId)
    }

    suspend fun addItineraryItem(
        adventureId: Long,
        title: String,
        stageId: Long?,
    ) {
        if (title.isBlank()) return
        dao.insertItineraryItem(
            ItineraryItemEntity(
                adventureId = adventureId,
                stageId = stageId,
                title = title.trim(),
                position = dao.nextItineraryPosition(adventureId),
            ),
        )
    }

    suspend fun setItineraryItemComplete(
        item: ItineraryItemEntity,
        complete: Boolean,
    ) {
        dao.updateItineraryItem(
            item.copy(
                isComplete = complete,
                completedAtEpochMillis = if (complete) System.currentTimeMillis() else null,
            ),
        )
    }

    suspend fun deleteItineraryItem(item: ItineraryItemEntity) {
        dao.deleteItineraryItem(item)
    }
}

data class AdventurePlanningSnapshot(
    val adventure: AdventureEntity,
    val stages: List<StageEntity>,
)

data class PlanningStageDraft(
    val persistedId: Long?,
    val title: String,
    val isComplete: Boolean,
    val completedAtEpochMillis: Long?,
)
