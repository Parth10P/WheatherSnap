package com.parth.weathersnap.ui.savedreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SavedReportsUiState - Represents the UI state for the Saved Reports screen.
 *
 * @param isLoading Whether reports are being loaded
 * @param reports List of saved weather reports
 * @param errorMessage Error message if something went wrong (nullable)
 */
data class SavedReportsUiState(
    val isLoading: Boolean = false,
    val reports: List<WeatherReportEntity> = emptyList(),
    val errorMessage: String? = null
)

/**
 * SavedReportsViewModel - ViewModel for the Saved Reports screen.
 *
 * Manages saved reports state using StateFlow.
 * Collects reports from the Room database via the repository.
 *
 * @param repository WeatherRepository for data operations
 */
@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedReportsUiState())
    val uiState: StateFlow<SavedReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    /** Load all saved reports from the local database */
    private fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllReports().collect { reports ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reports = reports
                )
            }
        }
    }

    // TODO: Add functions to delete reports, search, filter, etc.
}
