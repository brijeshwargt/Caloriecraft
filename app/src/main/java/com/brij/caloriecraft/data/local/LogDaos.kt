package com.brij.caloriecraft.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

data class DailyCalorieSummary(
    val entryDate: Date,
    val totalCalories: Int
)

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog)

    @Query("SELECT * FROM food_logs WHERE entryDate BETWEEN :startDate AND :endDate ORDER BY entryDate DESC")
    fun getFoodLogsForDateRange(startDate: Date, endDate: Date): Flow<List<FoodLog>>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Int)

    @Query("""
        SELECT SUM(calories) as totalCalories, entryDate
        FROM food_logs
        GROUP BY strftime('%Y-%m-%d', entryDate / 1000, 'unixepoch')
        ORDER BY entryDate DESC
    """)
    fun getDailyCalorieSummary(): Flow<List<DailyCalorieSummary>>
}

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog)

    @Query("SELECT * FROM weight_logs WHERE entryDate BETWEEN :startDate AND :endDate ORDER BY entryDate ASC")
    fun getWeightLogsForDateRange(startDate: Date, endDate: Date): Flow<List<WeightLog>>
}