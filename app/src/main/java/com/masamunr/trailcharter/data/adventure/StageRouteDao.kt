package com.masamunr.trailcharter.data.adventure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StageRouteDao {
    @Transaction
    @Query("SELECT * FROM stage_routes WHERE stageId = :stageId LIMIT 1")
    fun observeStageRoute(stageId: Long): Flow<StageRouteAggregate?>

    @Transaction
    @Query("SELECT * FROM stage_routes WHERE stageId = :stageId LIMIT 1")
    suspend fun getStageRoute(stageId: Long): StageRouteAggregate?

    @Query("SELECT * FROM stage_routes WHERE stageId = :stageId LIMIT 1")
    suspend fun getStageRouteEntity(stageId: Long): StageRouteEntity?

    @Upsert
    suspend fun upsertStageRoute(route: StageRouteEntity)

    @Insert
    suspend fun insertControlPoints(points: List<StageRouteControlPointEntity>)

    @Insert
    suspend fun insertGeometryPoints(points: List<StageRouteGeometryPointEntity>)

    @Query("DELETE FROM stage_route_control_points WHERE stageId = :stageId")
    suspend fun deleteControlPoints(stageId: Long)

    @Query("DELETE FROM stage_route_geometry_points WHERE stageId = :stageId")
    suspend fun deleteGeometryPoints(stageId: Long)

    @Query("DELETE FROM stage_routes WHERE stageId = :stageId")
    suspend fun deleteStageRoute(stageId: Long)

    @Transaction
    suspend fun replaceStageRoute(
        route: StageRouteEntity,
        controlPoints: List<StageRouteControlPointEntity>,
        geometryPoints: List<StageRouteGeometryPointEntity>,
    ) {
        val existing = getStageRouteEntity(route.stageId)
        upsertStageRoute(
            route.copy(createdAtEpochMillis = existing?.createdAtEpochMillis ?: route.createdAtEpochMillis),
        )
        deleteControlPoints(route.stageId)
        deleteGeometryPoints(route.stageId)
        insertControlPoints(controlPoints)
        if (geometryPoints.isNotEmpty()) insertGeometryPoints(geometryPoints)
    }
}
