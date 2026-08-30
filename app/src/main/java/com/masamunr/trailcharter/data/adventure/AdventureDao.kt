package com.masamunr.trailcharter.data.adventure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdventureDao {
    @Query(
        """
        SELECT
            a.id,
            a.title,
            a.summary,
            a.startDateEpochDay,
            a.endDateEpochDay,
            (SELECT COUNT(*) FROM stages s WHERE s.adventureId = a.id) AS stageCount,
            (SELECT COUNT(*) FROM stages s WHERE s.adventureId = a.id AND s.isComplete = 1) AS completedStageCount
        FROM adventures a
        ORDER BY
            CASE WHEN a.startDateEpochDay IS NULL THEN 1 ELSE 0 END,
            a.startDateEpochDay ASC,
            a.updatedAtEpochMillis DESC
        """,
    )
    fun observeAdventureSummaries(): Flow<List<AdventureSummaryRow>>

    @Query("SELECT * FROM adventures WHERE id = :adventureId LIMIT 1")
    fun observeAdventure(adventureId: Long): Flow<AdventureEntity?>

    @Query("SELECT * FROM stages WHERE adventureId = :adventureId ORDER BY position ASC, id ASC")
    fun observeStages(adventureId: Long): Flow<List<StageEntity>>

    @Query("SELECT * FROM itinerary_items WHERE adventureId = :adventureId ORDER BY position ASC, id ASC")
    fun observeItineraryItems(adventureId: Long): Flow<List<ItineraryItemEntity>>

    @Query(
        """
        SELECT
            s.id AS stageId,
            s.adventureId AS adventureId,
            a.title AS adventureTitle,
            s.title AS stageTitle,
            s.position AS stagePosition,
            CASE WHEN r.stageId IS NULL THEN 0 ELSE 1 END AS hasRoute
        FROM stages s
        INNER JOIN adventures a ON a.id = s.adventureId
        LEFT JOIN stage_routes r ON r.stageId = s.id
        ORDER BY a.updatedAtEpochMillis DESC, s.position ASC, s.id ASC
        """,
    )
    fun observeRoutePlanningStages(): Flow<List<RoutePlanningStageRow>>

    @Insert
    suspend fun insertAdventure(adventure: AdventureEntity): Long

    @Update
    suspend fun updateAdventure(adventure: AdventureEntity)

    @Query("DELETE FROM adventures WHERE id = :adventureId")
    suspend fun deleteAdventure(adventureId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM stages WHERE adventureId = :adventureId")
    suspend fun nextStagePosition(adventureId: Long): Int

    @Insert
    suspend fun insertStage(stage: StageEntity): Long

    @Transaction
    suspend fun createAdventureWithStage(
        adventure: AdventureEntity,
        stageTitle: String,
    ): Long {
        val adventureId = insertAdventure(adventure)
        return insertStage(
            StageEntity(
                adventureId = adventureId,
                title = stageTitle,
                position = 0,
            ),
        )
    }

    @Update
    suspend fun updateStage(stage: StageEntity)

    @Delete
    suspend fun deleteStage(stage: StageEntity)

    @Query("DELETE FROM stages WHERE id = :stageId")
    suspend fun deleteStageById(stageId: Long)

    @Transaction
    suspend fun savePlanningSession(
        adventure: AdventureEntity,
        stagesToUpdate: List<StageEntity>,
        stageIdsToDelete: List<Long>,
        stagesToInsert: List<StageEntity>,
    ) {
        updateAdventure(adventure)
        stageIdsToDelete.forEach { deleteStageById(it) }
        stagesToUpdate.forEach { updateStage(it) }
        stagesToInsert.forEach { insertStage(it) }
    }

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM itinerary_items WHERE adventureId = :adventureId")
    suspend fun nextItineraryPosition(adventureId: Long): Int

    @Insert
    suspend fun insertItineraryItem(item: ItineraryItemEntity): Long

    @Update
    suspend fun updateItineraryItem(item: ItineraryItemEntity)

    @Delete
    suspend fun deleteItineraryItem(item: ItineraryItemEntity)
}
