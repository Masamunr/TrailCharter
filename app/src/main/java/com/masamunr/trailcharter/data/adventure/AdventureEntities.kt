package com.masamunr.trailcharter.data.adventure

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "adventures")
data class AdventureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String = "",
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "stages",
    foreignKeys = [
        ForeignKey(
            entity = AdventureEntity::class,
            parentColumns = ["id"],
            childColumns = ["adventureId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["adventureId"])],
)
data class StageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adventureId: Long,
    val title: String,
    val position: Int,
    val isComplete: Boolean = false,
    val completedAtEpochMillis: Long? = null,
)

@Entity(
    tableName = "itinerary_items",
    foreignKeys = [
        ForeignKey(
            entity = AdventureEntity::class,
            parentColumns = ["id"],
            childColumns = ["adventureId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["adventureId"]), Index(value = ["stageId"])],
)
data class ItineraryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adventureId: Long,
    val stageId: Long? = null,
    val title: String,
    val note: String = "",
    val position: Int,
    val isComplete: Boolean = false,
    val completedAtEpochMillis: Long? = null,
)

data class AdventureSummaryRow(
    val id: Long,
    val title: String,
    val summary: String,
    val startDateEpochDay: Long?,
    val endDateEpochDay: Long?,
    val stageCount: Int,
    val completedStageCount: Int,
)
