package com.parth.weathersnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * WeatherSnapDatabase - Room database for the WeatherSnap app.
 *
 * Contains the weather_reports table.
 * Version 1 is the initial schema.
 *
 * To add new tables:
 * 1. Create a new @Entity class
 * 2. Add it to the entities array below
 * 3. Create a DAO interface
 * 4. Add an abstract function returning the DAO
 * 5. Increment the version number and add a migration
 */
@Database(
    entities = [WeatherReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherSnapDatabase : RoomDatabase() {

    /** Provides access to the WeatherReport DAO */
    abstract fun weatherReportDao(): WeatherReportDao
}
