package com.parth.weathersnap.ui.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CreateReportUiState - UI state for the Create Report screen.
 *
 * Holds both the weather data (passed from WeatherScreen) and
 * user input (description, image).
 */
data class CreateReportUiState(
    // Weather data (pre-filled from WeatherScreen)
    val cityName: String = "",
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val pressure: Double = 0.0,
    val windSpeed: Double = 0.0,
    val weatherCondition: String = "",

    // User input
    val description: String = "",

    // Image data (set after CameraScreen capture)
    val imagePath: String? = null,
    val originalImageSize: Long = 0L,
    val compressedImageSize: Long = 0L,

    // Operation state
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

/**
 * CreateReportViewModel - Manages report creation logic.
 *
 * Receives weather data via SavedStateHandle (from navigation arguments),
 * handles user description input, image attachment from camera,
 * and saves the complete report to Room database.
 */
@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill weather data from navigation arguments
        val cityName = savedStateHandle.get<String>("cityName") ?: ""
        val temperature = savedStateHandle.get<String>("temperature")?.toDoubleOrNull() ?: 0.0
        val humidity = savedStateHandle.get<String>("humidity")?.toIntOrNull() ?: 0
        val pressure = savedStateHandle.get<String>("pressure")?.toDoubleOrNull() ?: 0.0
        val windSpeed = savedStateHandle.get<String>("windSpeed")?.toDoubleOrNull() ?: 0.0
        val weatherCondition = savedStateHandle.get<String>("weatherCondition") ?: ""

        _uiState.update {
            it.copy(
                cityName = cityName,
                temperature = temperature,
                humidity = humidity,
                pressure = pressure,
                windSpeed = windSpeed,
                weatherCondition = weatherCondition
            )
        }
    }

    /** Update the description text field */
    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Set the captured image data (called when returning from CameraScreen).
     */
    fun setImageData(path: String, originalSize: Long, compressedSize: Long) {
        _uiState.update {
            it.copy(
                imagePath = path,
                originalImageSize = originalSize,
                compressedImageSize = compressedSize
            )
        }
    }

    /**
     * Save the weather report to Room database.
     * Validates that description is not empty before saving.
     */
    fun saveReport() {
        val state = _uiState.value

        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please add a description") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val report = WeatherReportEntity(
                    cityName = state.cityName,
                    temperature = state.temperature,
                    humidity = state.humidity,
                    pressure = state.pressure,
                    windSpeed = state.windSpeed,
                    weatherCondition = state.weatherCondition,
                    description = state.description,
                    imagePath = state.imagePath,
                    originalImageSize = state.originalImageSize,
                    compressedImageSize = state.compressedImageSize
                )
                repository.saveReport(report)
                _uiState.update {
                    it.copy(isLoading = false, isSaved = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save report: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /** Clear error message */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
