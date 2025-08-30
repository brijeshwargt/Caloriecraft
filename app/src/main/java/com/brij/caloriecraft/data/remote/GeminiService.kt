package com.brij.caloriecraft.data.remote

import android.util.Log
import com.brij.caloriecraft.BuildConfig
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

            response.text?.let { rawJsonText ->
                // Clean the raw text to extract pure JSON
                val cleanedJsonText = extractJsonFromMarkdown(rawJsonText)

                if (cleanedJsonText.isBlank()) {
                    Log.e("GeminiService", "Cleaned JSON text is blank. Original API response: $rawJsonText")
                    return Result.failure(Exception("Empty or invalid JSON content after cleaning API response."))
                }

                Log.d("GeminiService", "Cleaned JSON for parsing: $cleanedJsonText")
                try {
                    val parsedResponse = json.decodeFromString<GeminiResponse>(cleanedJsonText)
                    Result.success(parsedResponse)
                } catch (e: Exception) {
                    Log.e("GeminiService", "Error decoding JSON: ${e.message}. Cleaned JSON: $cleanedJsonText", e)
                    Result.failure(Exception("Failed to decode cleaned JSON: ${e.message}"))
                }
            } ?: Result.failure(Exception("Empty response text from API"))
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API or during response processing", e)
            Result.failure(Exception("Failed to parse food input due to API error or processing issue: ${e.message}"))
        }
    }

    private fun extractJsonFromMarkdown(text: String): String {
        val trimmedText = text.trim()
        val markdownJsonRegex =
            """^(?:```(?:json)?\s*)?(\{.*\})(?:\s*```)?$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matchResult = markdownJsonRegex.find(trimmedText)
        if (matchResult != null && matchResult.groupValues.size > 1) {
            return matchResult.groupValues[1].trim()
        }
        if (trimmedText.startsWith("{") && trimmedText.endsWith("}")) {
            return trimmedText
        }
        Log.w("GeminiService", "Could not extract JSON from markdown. Raw text: $text")
        return trimmedText
    }
}