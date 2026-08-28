package com.masamunr.trailcharter.data.adventure

import kotlinx.coroutines.flow.Flow

class AdventureRepository(
    private val dao: AdventureDao,
) {
    fun observeAdventureSummaries(): Flow<List<AdventureSummaryRow>> = dao.observeAdventureSummaries()

    fun observeAdventure(adventureId: Long): Flow<AdventureEntity?> = dao.observeAdventure(adventureId)

    fun observeStages(adventureId: Long): Flow<List<StageEntity>> = dao.observeStages(adventureId)

    fun observeItineraryItems(adventureId: Long): Flow<List<ItineraryItemEntity>> =
        dao.observeItineraryItems(adventureId)

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

    suspend fun updateAdventure(
        adventure: AdventureEntity,
        title: String,
        summary: String,
        startDateEpochDay: Long?,
        endDateEpochDay: Long?,
    ) {
        require(title.isNotBlank()) { "Adventure title is required" }
        dao.updateAdventure(
            adventure.copy(
                title = title.trim(),
                summary = summary.trim(),
                startDateEpochDay = startDateEpochDay,
                endDateEpochDay = endDateEpochDay,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteAdventure(adventureId: Long) {
        dao.deleteAdventure(adventureId)
    }

    suspend fun addStage(adventureId: Long, title: String) {
        if (title.isBlank()) return
        dao.insertStage(
            StageEntity(
                adventureId = adventureId,
                title = title.trim(),
                position = dao.nextStagePosition(adventureId),
            ),
        )
    }

    suspend fun deleteStage(stage: StageEntity) {
        dao.deleteStage(stage)
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
