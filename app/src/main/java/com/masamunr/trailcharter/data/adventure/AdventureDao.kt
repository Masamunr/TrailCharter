package com.masamunr.trailcharter.data.adventure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
            (SELECT COUNT(*) FROM itinerary_items i WHERE i.adventureId = a.id) AS itineraryCount,
            (SELECT COUNT(*) FROM itinerary_items i WHERE i.adventureId = a.id AND i.isComplete = 1) AS completedCount
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

    @Delete
    suspend fun deleteStage(stage: StageEntity)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM itinerary_items WHERE adventureId = :adventureId")
    suspend fun nextItineraryPosition(adventureId: Long): Int

    @Insert
    suspend fun insertItineraryItem(item: ItineraryItemEntity): Long

    @Update
    suspend fun updateItineraryItem(item: ItineraryItemEntity)

    @Delete
    suspend fun deleteItineraryItem(item: ItineraryItemEntity)
}
