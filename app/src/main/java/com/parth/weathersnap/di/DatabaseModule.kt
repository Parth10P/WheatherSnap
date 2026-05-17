package com.parth.weathersnap.di

import android.content.Context
import androidx.room.Room
import com.parth.weathersnap.data.local.WeatherReportDao
import com.parth.weathersnap.data.local.WeatherSnapDatabase
import com.parth.weathersnap.utils.Constants
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

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WeatherSnapDatabase {
        return Room.databaseBuilder(
            context,
            WeatherSnapDatabase::class.java,
            Constants.DATABASE_NAME
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
