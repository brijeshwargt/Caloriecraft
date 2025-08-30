package com.brij.caloriecraft.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FoodLog::class, WeightLog::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CalorieCraftDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: CalorieCraftDatabase? = null

        fun getDatabase(context: Context): CalorieCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalorieCraftDatabase::class.java,
                    "calorie_craft_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}