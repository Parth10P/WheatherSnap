package com.parth.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.weathersnap.data.remote.GeocodingResult
import com.parth.weathersnap.data.remote.getWeatherCondition
import com.parth.weathersnap.data.repository.WeatherRepository
import com.parth.weathersnap.utils.formattedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherDetails(
    val cityName: String,
    val temperature: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Double,
    val weatherCondition: String
)

sealed interface WeatherContentState {
    data object Empty : WeatherContentState
    data object Loading : WeatherContentState
    data class Success(val weather: WeatherDetails) : WeatherContentState
}

data class WeatherUiState(
    val searchQuery: String = "",
    val suggestions: List<GeocodingResult> = emptyList(),
    val showSuggestions: Boolean = false,
    val isSearching: Boolean = false,
    val selectedCity: GeocodingResult? = null,
    val weatherState: WeatherContentState = WeatherContentState.Empty,
    val errorMessage: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                selectedCity = null,
                isSearching = query.length >= 3
            )
        }

        searchJob?.cancel()

        if (query.length < 3) {
            _uiState.update {
                it.copy(
                    suggestions = emptyList(),
                    showSuggestions = false,
                    isSearching = false
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(250)
            runCatching { repository.searchCities(query) }
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            suggestions = results,
                            showSuggestions = results.isNotEmpty(),
                            isSearching = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            suggestions = emptyList(),
                            showSuggestions = false,
                            isSearching = false
                        )
                    }
                }
        }
    }

    fun onSearchRequested() {
        val current = _uiState.value
        val selected = current.selectedCity
        if (selected != null && current.searchQuery == selected.formattedName()) {
            fetchWeather(selected)
            return
        }

        val query = current.searchQuery.trim()
        if (query.length < 3) {
            _uiState.update { it.copy(errorMessage = "Enter at least 3 characters to search.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearching = true,
                    suggestions = emptyList(),
                    showSuggestions = false
                )
            }
            runCatching { repository.searchCities(query) }
                .onSuccess { results ->
                    val firstResult = results.firstOrNull()
                    if (firstResult == null) {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                weatherState = WeatherContentState.Empty,
                                errorMessage = "No matching cities found."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                suggestions = results,
                                selectedCity = firstResult,
                                searchQuery = firstResult.formattedName()
                            )
                        }
                        fetchWeather(firstResult)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = error.localizedMessage ?: "Unable to search for cities."
                        )
                    }
                }
        }
    }

    fun onCitySelected(city: GeocodingResult) {
        _uiState.update {
            it.copy(
                searchQuery = city.formattedName(),
                selectedCity = city,
                suggestions = emptyList(),
                showSuggestions = false
            )
        }
        fetchWeather(city)
    }

    fun dismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fetchWeather(city: GeocodingResult) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showSuggestions = false,
                    isSearching = false,
                    weatherState = WeatherContentState.Loading,
                    errorMessage = null
                )
            }

            runCatching { repository.getCurrentWeather(city.latitude, city.longitude) }
                .onSuccess { response ->
                    val current = response.current
                    if (current == null) {
                        _uiState.update {
                            it.copy(
                                weatherState = WeatherContentState.Empty,
                                errorMessage = "Weather data is unavailable for this city."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                selectedCity = city,
                                weatherState = WeatherContentState.Success(
                                    WeatherDetails(
                                        cityName = city.formattedName(),
                                        temperature = current.temperature,
                                        humidity = current.humidity,
                                        pressure = current.pressure,
                                        windSpeed = current.windSpeed,
                                        weatherCondition = getWeatherCondition(current.weatherCode)
                                    )
                                )
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            weatherState = WeatherContentState.Empty,
                            errorMessage = error.localizedMessage ?: "Unable to load weather details."
                        )
                    }
                }
        }
    }
}
