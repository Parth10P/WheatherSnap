package com.parth.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WeatherReportEntity - Room database entity representing a saved weather report.
 *
 * This entity stores weather report data locally, including the weather info,
 * user description, optional image path, and a timestamp.
 *
 * @param id Auto-generated primary key
 * @param title Report title
 * @param description User's weather description
 * @param temperature Temperature at the time of report
 * @param weatherCondition Weather condition (e.g., "Sunny", "Rainy")
 * @param imagePath Local file path to captured photo (nullable)
 * @param timestamp Time when the report was created
 */
@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val temperature: Double,
    val weatherCondition: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
