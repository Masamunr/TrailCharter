package com.masamunr.trailcharter.data.adventure

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AdventureEntity::class,
        StageEntity::class,
        ItineraryItemEntity::class,
        StageRouteEntity::class,
        StageRouteControlPointEntity::class,
        StageRouteGeometryPointEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class TrailCharterDatabase : RoomDatabase() {
    abstract fun adventureDao(): AdventureDao
    abstract fun stageRouteDao(): StageRouteDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stages ADD COLUMN isComplete INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stages ADD COLUMN completedAtEpochMillis INTEGER")
            }
        }

        internal val MIGRATION_2_3_STATEMENTS = listOf(
            """
            CREATE TABLE IF NOT EXISTS `stage_routes` (
                `stageId` INTEGER NOT NULL,
                `planningMode` TEXT NOT NULL,
                `travelMode` TEXT NOT NULL,
                `snapToNetwork` INTEGER NOT NULL,
                `distanceMetres` REAL,
                `ascentMetres` REAL,
                `descentMetres` REAL,
                `durationSeconds` INTEGER,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`stageId`),
                FOREIGN KEY(`stageId`) REFERENCES `stages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `stage_route_control_points` (
                `stageId` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                `role` TEXT NOT NULL,
                `latitude` REAL NOT NULL,
                `longitude` REAL NOT NULL,
                PRIMARY KEY(`stageId`, `position`),
                FOREIGN KEY(`stageId`) REFERENCES `stage_routes`(`stageId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `stage_route_geometry_points` (
                `stageId` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                `latitude` REAL NOT NULL,
                `longitude` REAL NOT NULL,
                PRIMARY KEY(`stageId`, `position`),
                FOREIGN KEY(`stageId`) REFERENCES `stage_routes`(`stageId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3_STATEMENTS.forEach(db::execSQL)
            }
        }

        @Volatile
        private var instance: TrailCharterDatabase? = null

        fun getInstance(context: Context): TrailCharterDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrailCharterDatabase::class.java,
                    "trailcharter.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
