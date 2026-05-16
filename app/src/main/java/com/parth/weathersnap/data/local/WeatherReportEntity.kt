package com.parth.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WeatherReportEntity - Room database entity representing a saved weather report.
 *
 * Stores the weather snapshot data along with user notes and an optional photo.
 * The photo path points to a COMPRESSED image saved in the app's internal storage.
 *
 * @param id Auto-generated primary key
 * @param cityName Name of the city
 * @param temperature Temperature in °C
 * @param humidity Humidity percentage
 * @param pressure Atmospheric pressure in hPa
 * @param windSpeed Wind speed in km/h
 * @param weatherCondition Human-readable weather condition (e.g., "Sunny")
 * @param description User's notes/description about the weather
 * @param imagePath Local file path to compressed captured photo (nullable)
 * @param originalImageSize Original image size in bytes before compression
 * @param compressedImageSize Compressed image size in bytes after compression
 * @param timestamp Time when the report was created (epoch millis)
 */
@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Double,
    val weatherCondition: String,
    val description: String,
    val imagePath: String? = null,
    val originalImageSize: Long = 0L,
    val compressedImageSize: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
