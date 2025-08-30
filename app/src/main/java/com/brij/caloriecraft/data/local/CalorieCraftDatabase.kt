package com.brij.caloriecraft.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
                val MIGRATION_1_2 = object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE food_logs ADD COLUMN protein REAL NOT NULL DEFAULT 0.0")
                        database.execSQL("ALTER TABLE food_logs ADD COLUMN carbs REAL NOT NULL DEFAULT 0.0")
                        database.execSQL("ALTER TABLE food_logs ADD COLUMN fibers REAL NOT NULL DEFAULT 0.0")
                        database.execSQL("ALTER TABLE food_logs ADD COLUMN fats REAL NOT NULL DEFAULT 0.0")
                    }
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalorieCraftDatabase::class.java,
                    "calorie_craft_database"
                ).addMigrations(MIGRATION_1_2)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}