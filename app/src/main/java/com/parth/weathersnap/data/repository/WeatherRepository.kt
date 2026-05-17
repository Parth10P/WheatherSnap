package com.parth.weathersnap.data.repository

import com.parth.weathersnap.data.local.WeatherReportDao
import com.parth.weathersnap.data.local.WeatherReportEntity
import com.parth.weathersnap.data.remote.GeocodingApiService
import com.parth.weathersnap.data.remote.GeocodingResult
import com.parth.weathersnap.data.remote.WeatherApiService
import com.parth.weathersnap.data.remote.WeatherResponse
import com.parth.weathersnap.di.IoDispatcher
import com.parth.weathersnap.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val weatherReportDao: WeatherReportDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val cityCache = LinkedHashMap<String, List<GeocodingResult>>(
        Constants.MAX_CACHED_CITIES,
        0.75f,
        true
    )

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResponse =
        withContext(ioDispatcher) {
            weatherApiService.getCurrentWeather(latitude, longitude)
        }

    suspend fun searchCities(query: String): List<GeocodingResult> = withContext(ioDispatcher) {
        val key = query.lowercase().trim()
        if (key.isBlank()) {
            return@withContext emptyList()
        }

        cityCache[key]?.let { return@withContext it }

        val results = geocodingApiService.searchCities(
            name = key,
            count = Constants.MAX_CITY_RESULTS
        ).results.orEmpty()

        if (cityCache.size >= Constants.MAX_CACHED_CITIES) {
            cityCache.remove(cityCache.keys.first())
        }
        cityCache[key] = results
        results
    }

    fun getAllReports(): Flow<List<WeatherReportEntity>> =
        weatherReportDao.getAllReports().flowOn(ioDispatcher)

    suspend fun saveReport(report: WeatherReportEntity): Long = withContext(ioDispatcher) {
        weatherReportDao.insertReport(report)
    }

    suspend fun deleteReport(report: WeatherReportEntity) = withContext(ioDispatcher) {
        weatherReportDao.deleteReport(report)
    }

    suspend fun deleteAllReports() = withContext(ioDispatcher) {
        weatherReportDao.deleteAllReports()
    }
}
