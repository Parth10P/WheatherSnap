package com.parth.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * WeatherUiState - Represents the UI state for the Weather screen.
 *
 * @param isLoading Whether data is currently being loaded
 * @param temperature Current temperature (placeholder)
 * @param weatherCondition Current weather condition text
 * @param cityName Name of the city
 * @param errorMessage Error message if something went wrong (nullable)
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val temperature: Double = 0.0,
    val weatherCondition: String = "",
    val cityName: String = "",
    val errorMessage: String? = null
)

/**
 * WeatherViewModel - ViewModel for the Weather screen.
 *
 * Manages the weather data state using StateFlow.
 * Business logic will be added here when implementing weather API calls.
 *
 * @param repository WeatherRepository for data operations
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // TODO: Add functions to fetch weather data, handle location, etc.
}
