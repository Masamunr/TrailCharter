package com.masamunr.trailcharter.data.adventure

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AdventureEntity::class, StageEntity::class, ItineraryItemEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class TrailCharterDatabase : RoomDatabase() {
    abstract fun adventureDao(): AdventureDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stages ADD COLUMN isComplete INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stages ADD COLUMN completedAtEpochMillis INTEGER")
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
