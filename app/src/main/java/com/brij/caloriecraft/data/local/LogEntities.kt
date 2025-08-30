package com.brij.caloriecraft.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String, // e.g., "Cooked Rice"
    val calories: Int,
    val quantity: Double, // e.g., 200.0
    val unit: String, // e.g., "g", "ml", "pcs"
    val entryDate: Date,
    val protein: Double = 0.0, // grams
    val carbs: Double = 0.0,   // grams
    val fibers: Double = 0.0,  // grams
    val fats: Double = 0.0     // grams
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weightInKg: Double,
    val entryDate: Date
)