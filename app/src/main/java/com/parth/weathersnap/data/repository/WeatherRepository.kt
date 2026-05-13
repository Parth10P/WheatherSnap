package com.parth.weathersnap.data.repository

import com.parth.weathersnap.data.local.WeatherReportDao
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.remote.WeatherApiService
import com.parth.weathersnap.data.remote.WeatherResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WeatherRepository - Single source of truth for weather data.
 *
 * This repository mediates between the remote API (Retrofit) and
 * the local database (Room). ViewModels should only interact with
 * this repository, never directly with the API or database.
 *
 * @param weatherApiService Retrofit service for API calls
 * @param weatherReportDao Room DAO for local database operations
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val weatherReportDao: WeatherReportDao
) {
    // ==================== Remote (API) Operations ====================

    /**
     * Fetch current weather from the remote API.
     *
     * TODO: Implement proper error handling and caching
     */
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): WeatherResponse {
        return weatherApiService.getCurrentWeather(latitude, longitude, apiKey)
    }

    // ==================== Local (Room) Operations ====================

    /** Get all saved reports as a reactive Flow */
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
