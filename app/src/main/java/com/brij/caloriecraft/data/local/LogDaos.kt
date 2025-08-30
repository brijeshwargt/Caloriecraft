package com.brij.caloriecraft.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog)

    @Query("SELECT * FROM food_logs WHERE entryDate BETWEEN :startDate AND :endDate ORDER BY entryDate DESC")
    fun getFoodLogsForDateRange(startDate: Date, endDate: Date): Flow<List<FoodLog>>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Int)
}

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog)

    @Query("SELECT * FROM weight_logs WHERE entryDate BETWEEN :startDate AND :endDate ORDER BY entryDate ASC")
    fun getWeightLogsForDateRange(startDate: Date, endDate: Date): Flow<List<WeightLog>>
}