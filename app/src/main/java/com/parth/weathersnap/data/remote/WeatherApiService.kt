package com.parth.weathersnap.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * WeatherApiService - Retrofit interface for Open-Meteo API calls.
 *
 * Open-Meteo provides TWO separate APIs:
 *   1. Weather API (api.open-meteo.com) - Get weather by lat/lon
 *   2. Geocoding API (geocoding-api.open-meteo.com) - Search cities by name
 *
 * No API key is required for either endpoint.
 */
interface WeatherApiService {

    /**
     * Fetch current weather data for a specific location.
     *
     * We request these "current" fields:
     *   - temperature_2m: Temperature at 2 meters above ground
     *   - relative_humidity_2m: Humidity percentage
     *   - surface_pressure: Atmospheric pressure in hPa
     *   - wind_speed_10m: Wind speed at 10 meters above ground
     *   - weather_code: WMO weather condition code
     *
     * @param latitude Latitude of the city
     * @param longitude Longitude of the city
     * @param current Comma-separated list of requested current weather variables
     * @return WeatherResponse containing current weather data
     */
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,surface_pressure,wind_speed_10m,weather_code"
    ): WeatherResponse
}

/**
 * GeocodingApiService - Retrofit interface for Open-Meteo Geocoding API.
 *
 * Used to search cities by name for autocomplete functionality.
 */
interface GeocodingApiService {

    /**
     * Search for cities by name.
     *
     * @param name City name to search for (partial match supported)
     * @param count Maximum number of results to return
     * @param language Language for results (default: English)
     * @return GeocodingResponse containing matching city results
     */
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String = "en"
    ): GeocodingResponse
}
