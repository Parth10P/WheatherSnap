package com.parth.weathersnap.ui.report

import androidx.lifecycle.ViewModel
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * CreateReportUiState - Represents the UI state for the Create Report screen.
 *
 * @param isLoading Whether a save operation is in progress
 * @param title Report title input
 * @param description Report description input
 * @param imagePath Path to captured image (nullable)
 * @param isSaved Whether the report was successfully saved
 * @param errorMessage Error message if something went wrong (nullable)
 */
data class CreateReportUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val imagePath: String? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

/**
 * CreateReportViewModel - ViewModel for the Create Report screen.
 *
 * Manages report creation state using StateFlow.
 * Business logic for saving reports will be added later.
 *
 * @param repository WeatherRepository for data operations
 */
@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    // TODO: Add functions to update fields, save report, handle image, etc.
}
