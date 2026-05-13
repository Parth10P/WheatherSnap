package com.parth.weathersnap.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * WeatherReportDao - Data Access Object for weather reports.
 *
 * Provides methods to interact with the weather_reports table.
 * Uses Flow for reactive data streams and suspend functions for one-shot operations.
 */
@Dao
interface WeatherReportDao {

    /** Get all reports ordered by newest first, as a reactive Flow */
    @Query("SELECT * FROM weather_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WeatherReportEntity>>

    /** Get a single report by its ID */
    @Query("SELECT * FROM weather_reports WHERE id = :id")
    suspend fun getReportById(id: Long): WeatherReportEntity?

    /** Insert a new report (replace on conflict) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeatherReportEntity): Long

    /** Delete a specific report */
    @Delete
    suspend fun deleteReport(report: WeatherReportEntity)

    /** Delete all reports */
    @Query("DELETE FROM weather_reports")
    suspend fun deleteAllReports()
}
