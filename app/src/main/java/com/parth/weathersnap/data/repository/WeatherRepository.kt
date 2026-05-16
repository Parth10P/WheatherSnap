package com.parth.weathersnap.data.repository

import com.parth.weathersnap.data.local.WeatherReportDao
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.remote.GeocodingApiService
import com.parth.weathersnap.data.remote.GeocodingResult
import com.parth.weathersnap.data.remote.WeatherApiService
import com.parth.weathersnap.data.remote.WeatherResponse
import com.parth.weathersnap.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WeatherRepository - Single source of truth for all data operations.
 *
 * Mediates between:
 *   - Remote: WeatherApiService (weather data) + GeocodingApiService (city search)
 *   - Local: WeatherReportDao (Room database for saved reports)
 *
 * ViewModels should ONLY interact with this repository, never with APIs or DAOs directly.
 *
 * Also manages an in-memory cache for city search suggestions to avoid
 * redundant API calls for the same search query.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val weatherReportDao: WeatherReportDao
) {

    // ==================== In-Memory City Cache ====================

    /**
     * Simple in-memory cache for city search results.
     * Key = search query (lowercase), Value = list of matching cities.
     * Avoids hitting the API for repeated searches.
     */
    private val cityCache = LinkedHashMap<String, List<GeocodingResult>>(
        Constants.MAX_CACHED_CITIES, 0.75f, true
    )

    // ==================== Remote (API) Operations ====================

    /**
     * Fetch current weather for a location.
     *
     * @param latitude City latitude
     * @param longitude City longitude
     * @return WeatherResponse with current conditions
     */
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {
        return weatherApiService.getCurrentWeather(latitude, longitude)
    }

    /**
     * Search cities by name with in-memory caching.
     *
     * First checks the local cache. If the query was already searched,
     * returns cached results instantly. Otherwise, calls the API and
     * caches the results for future use.
     *
     * @param query City name search string
     * @return List of matching cities
     */
    suspend fun searchCities(query: String): List<GeocodingResult> {
        val key = query.lowercase().trim()
        if (key.isBlank()) return emptyList()

        // Check cache first
        cityCache[key]?.let { return it }

        // Call API and cache the result
        val response = geocodingApiService.searchCities(name = query)
        val results = response.results ?: emptyList()

        // Evict oldest entries if cache is full
        if (cityCache.size >= Constants.MAX_CACHED_CITIES) {
            val oldestKey = cityCache.keys.firstOrNull()
            oldestKey?.let { cityCache.remove(it) }
        }
        cityCache[key] = results

        return results
    }

    // ==================== Local (Room) Operations ====================

    /** Get all saved reports as a reactive Flow (newest first) */
    fun getAllReports(): Flow<List<WeatherReportEntity>> {
        return weatherReportDao.getAllReports()
    }

    /** Get a single report by ID */
    suspend fun getReportById(id: Long): WeatherReportEntity? {
        return weatherReportDao.getReportById(id)
    }

    /** Save a new weather report to the local database */
    suspend fun saveReport(report: WeatherReportEntity): Long {
        return weatherReportDao.insertReport(report)
    }

    /** Delete a specific weather report */
    suspend fun deleteReport(report: WeatherReportEntity) {
        weatherReportDao.deleteReport(report)
    }

    /** Delete all saved reports */
    suspend fun deleteAllReports() {
        weatherReportDao.deleteAllReports()
    }
}
