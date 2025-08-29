package com.brij.caloriecraft.data.remote

import com.brij.caloriecrafter.BuildConfig
import com.google.ai.client.generativeai.*
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Data classes to model the expected JSON response from Gemini
@Serializable
data class ParsedFoodItem(
    val foodName: String,
    val calories: Int,
    val quantity: Double,
    val unit: String
)

@Serializable
data class GeminiResponse(
    val mealType: String,
    val items: List<ParsedFoodItem>
)

class GeminiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val json = Json { ignoreUnknownKeys = true } // For safe JSON parsing

    suspend fun parseFoodInput(userInput: String): Result<GeminiResponse> {
        val prompt = """
            Analyze the following user input describing a meal. Identify the meal type (Breakfast, Lunch, Dinner, or Snack) and break down each food item mentioned.
            For each item, provide the food name, estimated total calories for the given quantity, the quantity itself, and the unit (e.g., 'g', 'ml', 'pcs', 'slice', 'chapati').
            
            Return the response ONLY as a single, minified JSON object with the following structure:
            {"mealType": "...", "items": [{"foodName": "...", "calories": ..., "quantity": ..., "unit": "..."}, ...]}

            User input: "$userInput"
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.let { jsonText ->
                val parsedResponse = json.decodeFromString<GeminiResponse>(jsonText)
                Result.success(parsedResponse)
            } ?: Result.failure(Exception("Empty response from API"))
        } catch (e: Exception) {
            // Log the exception e
            Result.failure(Exception("Failed to parse food input: ${e.message}"))
        }
    }
}