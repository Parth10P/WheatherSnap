package com.parth.weathersnap.data.remote

/**
 * WeatherResponse - Data class representing the API response.
 *
 * This is a simplified placeholder structure. Update this to match
 * your actual weather API response format (e.g., OpenWeatherMap).
 *
 * TODO: Replace with actual API response model when implementing weather logic
 */
data class WeatherResponse(
    val name: String = "",
    val main: MainData = MainData(),
    val weather: List<WeatherData> = emptyList()
)

data class MainData(
    val temp: Double = 0.0,
    val humidity: Int = 0,
    val pressure: Int = 0
)

data class WeatherData(
    val id: Int = 0,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)
