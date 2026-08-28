package com.masamunr.trailcharter.data.adventure

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AdventureEntity::class, StageEntity::class, ItineraryItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TrailCharterDatabase : RoomDatabase() {
    abstract fun adventureDao(): AdventureDao

    companion object {
        @Volatile
        private var instance: TrailCharterDatabase? = null

        fun getInstance(context: Context): TrailCharterDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrailCharterDatabase::class.java,
                    "trailcharter.db",
                ).build().also { instance = it }
            }
    }
}
