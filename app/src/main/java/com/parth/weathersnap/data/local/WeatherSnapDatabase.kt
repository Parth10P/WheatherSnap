package com.parth.weathersnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * WeatherSnapDatabase - Room database for the WeatherSnap app.
 *
 * Contains the weather_reports table.
 * Version 2 adds humidity, pressure, windSpeed, originalImageSize, compressedImageSize fields.
 *
 * exportSchema = false disables schema export for simplicity.
 * In production, you'd set this to true and manage migration files.
 */
@Database(
    entities = [WeatherReportEntity::class],
    version = 2,
    exportSchema = false
)
abstract class WeatherSnapDatabase : RoomDatabase() {

    /** Provides access to the WeatherReport DAO */
    abstract fun weatherReportDao(): WeatherReportDao
}
