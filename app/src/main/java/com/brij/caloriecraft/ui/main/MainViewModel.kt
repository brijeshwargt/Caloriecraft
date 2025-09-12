package com.brij.caloriecraft.ui.main
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brij.caloriecraft.data.LogRepository
import com.brij.caloriecraft.data.local.FoodLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents the state of our main screen
data class MainUiState(
    val selectedDate: Date = Date(),
    val foodLogsForSelectedDate: List<FoodLog> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFibers: Double = 0.0,
    val totalFats: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(private val repository: LogRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Date())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _foodLogs = _selectedDate.flatMapLatest { date ->
        repository.getLogsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        _selectedDate,
        _foodLogs,
        _isLoading,
        _errorMessage
    ) { date, logs, isLoading, error ->
        MainUiState(
            selectedDate = date,
            foodLogsForSelectedDate = logs,
            totalCalories = logs.sumOf { it.calories },
            totalProtein = logs.sumOf { it.protein },
            totalCarbs = logs.sumOf { it.carbs },
            totalFibers = logs.sumOf { it.fibers },
            totalFats = logs.sumOf { it.fats },
            isLoading = isLoading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )


    fun parseAndLog(userInput: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.parseAndLogFood(userInput, _selectedDate.value)
            result.onFailure { error ->
                _errorMessage.value = error.message
            }
            _isLoading.value = false
        }
    }

    fun deleteFoodLog(log: FoodLog) {
        viewModelScope.launch {
            repository.deleteFoodLog(log)
        }
    }

    fun goToPreviousDay() {
        val calendar = Calendar.getInstance().apply {
            time = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, -1)
        }
        _selectedDate.value = calendar.time
    }

    fun goToNextDay() {
        val calendar = Calendar.getInstance().apply {
            time = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, 1)
        }
        // Prevent navigating to a future date
        if (!isFutureDate(calendar.time)) {
            _selectedDate.value = calendar.time
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        fun isFutureDate(date: Date): Boolean {
            val today = Calendar.getInstance()
            // Clear time part for accurate date comparison
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val selected = Calendar.getInstance()
            selected.time = date
            selected.set(Calendar.HOUR_OF_DAY, 0)
            selected.set(Calendar.MINUTE, 0)
            selected.set(Calendar.SECOND, 0)
            selected.set(Calendar.MILLISECOND, 0)

            return selected.after(today)
        }

        fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance()
            cal1.time = date1
            val cal2 = Calendar.getInstance()
            cal2.time = date2
            return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
        }
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