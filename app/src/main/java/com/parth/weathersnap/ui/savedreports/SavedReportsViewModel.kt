package com.parth.weathersnap.ui.savedreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SavedReportsUiState - UI state for the Saved Reports screen.
 */
data class SavedReportsUiState(
    val isLoading: Boolean = true,
    val reports: List<WeatherReportEntity> = emptyList(),
    val errorMessage: String? = null
)

/**
 * SavedReportsViewModel - Manages the list of saved weather reports.
 *
 * Observes Room database via Flow for reactive updates.
 * Supports deleting individual reports and clearing all reports.
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

    /**
     * Load all saved reports from Room, observing as a Flow.
     * Any changes to the database will automatically update the UI.
     */
    private fun loadReports() {
        viewModelScope.launch {
            repository.getAllReports()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load reports: ${e.localizedMessage}"
                        )
                    }
                }
                .collect { reports ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reports = reports,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /** Delete a single report */
    fun deleteReport(report: WeatherReportEntity) {
        viewModelScope.launch {
            try {
                // Also delete the image file if it exists
                report.imagePath?.let { path ->
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                }
                repository.deleteReport(report)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete report: ${e.localizedMessage}")
                }
            }
        }
    }

    /** Delete all reports */
    fun deleteAllReports() {
        viewModelScope.launch {
            try {
                // Delete all image files
                _uiState.value.reports.forEach { report ->
                    report.imagePath?.let { path ->
                        val file = java.io.File(path)
                        if (file.exists()) file.delete()
                    }
                }
                repository.deleteAllReports()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete reports: ${e.localizedMessage}")
                }
            }
        }
    }

    /** Clear error message */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
