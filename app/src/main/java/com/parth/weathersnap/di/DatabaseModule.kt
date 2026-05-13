package com.parth.weathersnap.di

import android.content.Context
import androidx.room.Room
import com.parth.weathersnap.data.local.WeatherReportDao
import com.parth.weathersnap.data.local.WeatherSnapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule - Hilt module providing Room database dependencies.
 *
 * @InstallIn(SingletonComponent::class) ensures these dependencies
 * live for the entire app lifecycle (application-scoped singletons).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     * Uses fallbackToDestructiveMigration for development convenience.
     *
     * TODO: Replace with proper migrations before production release
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WeatherSnapDatabase {
        return Room.databaseBuilder(
            context,
            WeatherSnapDatabase::class.java,
            "weathersnap_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Provides the WeatherReport DAO from the database instance */
    @Provides
    @Singleton
    fun provideWeatherReportDao(
        database: WeatherSnapDatabase
    ): WeatherReportDao {
        return database.weatherReportDao()
    }
}
