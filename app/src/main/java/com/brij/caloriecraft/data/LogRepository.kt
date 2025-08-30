package com.brij.caloriecraft.data

import com.brij.caloriecraft.data.local.FoodDao
import com.brij.caloriecraft.data.local.FoodLog
import com.brij.caloriecraft.data.local.WeightDao
import com.brij.caloriecraft.data.local.WeightLog
import com.brij.caloriecraft.data.remote.GeminiService
import java.util.Calendar
import java.util.Date

class LogRepository(
    private val foodDao: FoodDao,
    private val weightDao: WeightDao,
    private val geminiService: GeminiService
) {
    // Function to get logs for today
    fun getTodaysFoodLogs() = foodDao.getFoodLogsForDateRange(getStartOfToday(), getEndOfToday())

    suspend fun addWeightLog(weight: Double) {
        val log = WeightLog(weightInKg = weight, entryDate = Date())
        weightDao.insertWeightLog(log)
    }

    suspend fun deleteFoodLog(log: FoodLog) {
        foodDao.deleteFoodLogById(log.id)
    }

    suspend fun parseAndLogFood(userInput: String): Result<Unit> {
        val result = geminiService.parseFoodInput(userInput)
        return result.map { geminiResponse ->
            val entryDate = Date()
            geminiResponse.items.forEach { item ->
                val foodLog = FoodLog(
                    mealType = geminiResponse.mealType,
                    foodName = item.foodName,
                    calories = item.calories,
                    quantity = item.quantity,
                    unit = item.unit,
                    entryDate = entryDate,
                    protein = item.protein,
                    carbs = item.carbs,
                    fibers = item.fibers,
                    fats = item.fats
                )
                foodDao.insertFoodLog(foodLog)
            }
        }
    }

    // Helper functions for date queries
    private fun getStartOfToday(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun getEndOfToday(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
}