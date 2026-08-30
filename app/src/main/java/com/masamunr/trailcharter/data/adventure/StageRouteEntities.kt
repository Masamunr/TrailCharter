package com.masamunr.trailcharter.data.adventure

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "stage_routes",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StageRouteEntity(
    @PrimaryKey val stageId: Long,
    val planningMode: String,
    val travelMode: String,
    val snapToNetwork: Boolean,
    val distanceMetres: Double?,
    val ascentMetres: Double?,
    val descentMetres: Double?,
    val durationSeconds: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "stage_route_control_points",
    primaryKeys = ["stageId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = StageRouteEntity::class,
            parentColumns = ["stageId"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StageRouteControlPointEntity(
    val stageId: Long,
    val position: Int,
    val role: String,
    val latitude: Double,
    val longitude: Double,
)

@Entity(
    tableName = "stage_route_geometry_points",
    primaryKeys = ["stageId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = StageRouteEntity::class,
            parentColumns = ["stageId"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StageRouteGeometryPointEntity(
    val stageId: Long,
    val position: Int,
    val latitude: Double,
    val longitude: Double,
)

data class StageRouteAggregate(
    @Embedded val route: StageRouteEntity,
    @Relation(
        parentColumn = "stageId",
        entityColumn = "stageId",
    )
    val controlPoints: List<StageRouteControlPointEntity>,
    @Relation(
        parentColumn = "stageId",
        entityColumn = "stageId",
    )
    val geometryPoints: List<StageRouteGeometryPointEntity>,
)

data class RoutePlanningStageRow(
    val stageId: Long,
    val adventureId: Long,
    val adventureTitle: String,
    val stageTitle: String,
    val stagePosition: Int,
    val hasRoute: Boolean,
)
