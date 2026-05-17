package com.parth.weathersnap.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo Weather API Response Models.
 *
 * The Open-Meteo API returns weather data in a flat JSON structure.
 * We request specific fields via query params like:
 *   ?latitude=...&longitude=...&current=temperature_2m,relative_humidity_2m,...
 *
 * Example response:
 * {
 *   "current": {
 *     "temperature_2m": 22.5,
 *     "relative_humidity_2m": 65,
 *     "surface_pressure": 1013.2,
 *     "wind_speed_10m": 12.3,
 *     "weather_code": 3
 *   }
 * }
 */
data class WeatherResponse(
    val current: CurrentWeather? = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double = 0.0,

    @SerializedName("relative_humidity_2m")
    val humidity: Int = 0,

    @SerializedName("surface_pressure")
    val pressure: Double = 0.0,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double = 0.0,

    @SerializedName("weather_code")
    val weatherCode: Int = 0
)

// ==================== Geocoding API Response ====================

/**
 * Open-Meteo Geocoding API Response.
 *
 * Used for city name autocomplete/search.
 * Example: https://geocoding-api.open-meteo.com/v1/search?name=London&count=5
 */
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val id: Long = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    @SerializedName("country_code")
    val countryCode: String = "",
    val admin1: String? = null
)

// ==================== Weather Code Mapping ====================

/**
 * Maps Open-Meteo WMO weather codes to human-readable condition strings.
 *
 * Reference: https://open-meteo.com/en/docs
 * WMO Weather interpretation codes (WW):
 *  0 = Clear sky, 1-3 = Partly cloudy, 45-48 = Fog, etc.
 */
fun getWeatherCondition(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing Rain"
        71, 73, 75 -> "Snowfall"
        77 -> "Snow Grains"
        80, 81, 82 -> "Rain Showers"
        85, 86 -> "Snow Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with Hail"
        else -> "Unknown"
    }
}
