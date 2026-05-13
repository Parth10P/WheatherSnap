package com.parth.weathersnap.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * WeatherApiService - Retrofit interface for weather API calls.
 *
 * Define your API endpoints here. Currently a placeholder structure.
 * You will add the actual API base URL and endpoints when implementing
 * the weather API logic.
 *
 * Example usage with OpenWeatherMap API:
 *   Base URL: "https://api.openweathermap.org/data/2.5/"
 */
interface WeatherApiService {

    /**
     * Placeholder endpoint for fetching current weather data.
     *
     * TODO: Replace with actual API endpoint and response model
     *
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param apiKey Your weather API key
     * @return WeatherResponse (to be created)
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
