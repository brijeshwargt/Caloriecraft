package com.brij.caloriecraft.ui.history
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brij.caloriecraft.data.LogRepository
import com.brij.caloriecraft.data.local.DailyCalorieSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents the state for our History screen
data class HistoryUiState(
    val dailySummaries: List<DailyCalorieSummary> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(private val repository: LogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // As soon as the ViewModel is created, start observing the daily summaries
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDailyCalorieSummary().collect { summaries ->
                _uiState.update {
                    it.copy(
                        dailySummaries = summaries,
                        isLoading = false
                    )
                }
            }
        }
    }
}

// A factory is needed to create the ViewModel with its repository dependency
class HistoryViewModelFactory(private val repository: LogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}