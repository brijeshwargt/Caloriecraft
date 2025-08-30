package com.brij.caloriecraft.ui.main
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brij.caloriecraft.data.LogRepository
import com.brij.caloriecraft.data.local.FoodLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents the state of our main screen
data class MainUiState(
    val todaysLogs: List<FoodLog> = emptyList(),
    val totalCaloriesToday: Int = 0,
    val totalProteinToday: Double = 0.0,
    val totalCarbsToday: Double = 0.0,
    val totalFibersToday: Double = 0.0,
    val totalFatsToday: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(private val repository: LogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe changes to today's food logs from the database
        viewModelScope.launch {
            repository.getTodaysFoodLogs().collect { logs ->
                _uiState.update { currentState ->
                    currentState.copy(
                        todaysLogs = logs,
                        totalCaloriesToday = logs.sumOf { it.calories },
                        totalProteinToday = logs.sumOf { it.protein },
                        totalCarbsToday = logs.sumOf { it.carbs },
                        totalFibersToday = logs.sumOf { it.fibers },
                        totalFatsToday = logs.sumOf { it.fats }
                    )
                }
            }
        }
    }

    fun deleteFoodLog(log: FoodLog) {
        viewModelScope.launch {
            repository.deleteFoodLog(log)
        }
    }

    fun parseAndLog(userInput: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.parseAndLogFood(userInput)
            result.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

// Factory to create the ViewModel with its dependency (the repository)
class MainViewModelFactory(private val repository: LogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}