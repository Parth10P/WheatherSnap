package com.parth.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.data.remote.CurrentWeather
import com.parth.weathersnap.data.remote.GeocodingResult
import com.parth.weathersnap.data.remote.getWeatherCondition
import com.parth.weathersnap.data.remote.getWeatherEmoji
import com.parth.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WeatherUiState - Represents the complete UI state for the Weather screen.
 *
 * This single state object drives the entire screen. Compose observes this via
 * collectAsState() and recomposes when any field changes.
 */
data class WeatherUiState(
    // Search state
    val searchQuery: String = "",
    val suggestions: List<GeocodingResult> = emptyList(),
    val showSuggestions: Boolean = false,

    // Weather data state
    val cityName: String = "",
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val pressure: Double = 0.0,
    val windSpeed: Double = 0.0,
    val weatherCondition: String = "",
    val weatherEmoji: String = "🌡️",
    val weatherCode: Int = -1,

    // Selected city coordinates (needed for report creation)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Loading/Error states
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val hasWeatherData: Boolean = false,
    val errorMessage: String? = null
)

/**
 * WeatherViewModel - Manages weather search and display logic.
 *
 * Handles:
 *   1. City name search with debounce (waits 300ms after user stops typing)
 *   2. Displaying autocomplete suggestions from Open-Meteo Geocoding API
 *   3. Fetching weather data when user selects a city
 *   4. Managing loading/error/success states
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Reference to the current search job so we can cancel it on new input (debounce)
    private var searchJob: Job? = null

    /**
     * Called when the user types in the search field.
     *
     * Uses debounce: waits 300ms after the last keystroke before searching.
     * This prevents flooding the API with requests on every character typed.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel the previous search job if user is still typing
        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), showSuggestions = false) }
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce: wait 300ms before searching
            delay(300)
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = repository.searchCities(query)
                _uiState.update {
                    it.copy(
                        suggestions = results,
                        showSuggestions = results.isNotEmpty(),
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSearching = false, showSuggestions = false)
                }
            }
        }
    }

    /**
     * Called when the user selects a city from the suggestions dropdown.
     * Fetches weather data for that city's coordinates.
     */
    fun onCitySelected(city: GeocodingResult) {
        // Update search field with selected city name and hide suggestions
        val displayName = buildString {
            append(city.name)
            city.admin1?.let { append(", $it") }
            if (city.country.isNotBlank()) append(", ${city.country}")
        }
        _uiState.update {
            it.copy(
                searchQuery = displayName,
                showSuggestions = false,
                suggestions = emptyList()
            )
        }

        // Fetch weather for this city
        fetchWeather(city.name, city.latitude, city.longitude)
    }

    /**
     * Dismiss the suggestions dropdown (e.g., when user taps elsewhere).
     */
    fun dismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    /**
     * Fetch current weather data from Open-Meteo API.
     */
    private fun fetchWeather(cityName: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = repository.getCurrentWeather(lat, lon)
                val current = response.current

                if (current != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasWeatherData = true,
                            cityName = cityName,
                            temperature = current.temperature,
                            humidity = current.humidity,
                            pressure = current.pressure,
                            windSpeed = current.windSpeed,
                            weatherCode = current.weatherCode,
                            weatherCondition = getWeatherCondition(current.weatherCode),
                            weatherEmoji = getWeatherEmoji(current.weatherCode),
                            latitude = lat,
                            longitude = lon,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No weather data available for this location"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to fetch weather: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Clear error message after it's been shown to the user.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
